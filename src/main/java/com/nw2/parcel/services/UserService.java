package com.nw2.parcel.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.nw2.parcel.Dtos.SignUpRequest;
import com.nw2.parcel.Dtos.LoginResponse;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.entity.Dorm;
import com.nw2.parcel.exception.EmailAlreadyExistsException;
import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.repositories.DormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final DormRepository dormRepository;

    public LoginResponse signUp(SignUpRequest req) throws Exception {
        // ✅ 0) Normalize email ก่อน
        String normalizedEmail = req.getEmail().trim().toLowerCase();

        // ✅ 1) เช็คใน DB ก่อนเลย
        if (usersRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email is already in use.");
        }

        // ✅ 2) สร้างผู้ใช้ใน Firebase (Firebase ก็กัน email ซ้ำด้วย)
        UserRecord userRecord;
        try {
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(normalizedEmail)
                    .setPassword(req.getPassword())
                    .setDisplayName(req.getFirstName() + " " + req.getLastName());

            userRecord = FirebaseAuth.getInstance().createUser(createRequest);

        } catch (FirebaseAuthException e) {
            // ถ้า Firebase แจ้งว่า email ซ้ำ
            if ("EMAIL_ALREADY_EXISTS".equals(e.getErrorCode())) {
                throw new EmailAlreadyExistsException("Email is already in use.");
            }
            throw e; // error อื่นก็ปล่อยให้ handler 500 จัดการ
        }

        // 3) ถ้าเป็น RESIDENT ต้องมี dorm
        Dorm dorm = null;
        if ("RESIDENT".equalsIgnoreCase(req.getRole())) {
            if (req.getDormId() == null) {
                throw new IllegalArgumentException("dormId is required for RESIDENT.");
            }
            dorm = dormRepository.findById(req.getDormId())
                    .orElseThrow(() -> new IllegalArgumentException("Dorm not found: " + req.getDormId()));
        }

        // 4) บันทึก Users ใน DB
        Users user = new Users();
        user.setFirebaseUid(userRecord.getUid());
        user.setEmail(normalizedEmail);
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRole(Users.Role.valueOf(req.getRole().toUpperCase()));
        user.setStatus(Users.Status.ACTIVE);
        user.setPosition(req.getPosition());
        user.setRoomNumber(req.getRoomNumber());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (dorm != null) {
            user.setDorm(dorm);
        }

        try {
            usersRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            // กันเคส race condition ที่หลุด unique constraint DB
            throw new EmailAlreadyExistsException("Email is already in use.");
        }

        // 5) ตอบกลับ
        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getUserId());
        resp.setFirebaseUid(userRecord.getUid());
        resp.setEmail(normalizedEmail);
        resp.setFirstName(req.getFirstName());
        resp.setLastName(req.getLastName());
        resp.setRole(req.getRole());
        resp.setPosition(req.getPosition());
        resp.setDormId(dorm != null ? dorm.getDormId() : null);
        resp.setDormName(dorm != null ? dorm.getDormName() : null);
        resp.setRoomNumber(req.getRoomNumber());
        resp.setMessage("Signup successful");
        return resp;
    }

    public LoginResponse login(String idToken, FirebaseService firebaseService) throws Exception {
        var decoded = firebaseService.verifyIdToken(idToken);
        final String uid = decoded.getUid();
        final String emailFromToken = decoded.getEmail();

        Users u = usersRepository.findByFirebaseUid(uid)
                .orElseThrow(() -> new IllegalArgumentException("Please register before login"));

        LoginResponse resp = new LoginResponse();
        resp.setUserId(u.getUserId());
        resp.setFirebaseUid(uid);
        resp.setEmail(u.getEmail() != null ? u.getEmail() : emailFromToken);
        resp.setFirstName(u.getFirstName());
        resp.setLastName(u.getLastName());
        resp.setRole(u.getRole() != null ? u.getRole().name() : null);
        resp.setPosition(u.getPosition());

        if (u.getDorm() != null) {
            resp.setDormId(u.getDorm().getDormId());               // ⬅️ ส่งเป็น dormId
            resp.setDormName(u.getDorm().getDormName());           // (ถ้าต้องการแสดงชื่อด้วย)
        }
        resp.setRoomNumber(u.getRoomNumber());
        resp.setMessage("Login successful");
        return resp;
    }
}
