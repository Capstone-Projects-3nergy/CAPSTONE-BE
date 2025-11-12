package com.nw2.parcel.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.nw2.parcel.Dtos.SignUpRequest;
import com.nw2.parcel.Dtos.LoginResponse;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.entity.Dorm;
import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.repositories.DormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final DormRepository dormRepository;

    public LoginResponse signUp(SignUpRequest req) throws Exception {
        // 1) สร้างผู้ใช้ใน Firebase
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(req.getEmail())
                .setPassword(req.getPassword())
                .setDisplayName(req.getFirstName() + " " + req.getLastName());

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);

        // 2) ถ้าเป็น RESIDENT ต้องมี dormId และต้องหา Dorm ให้เจอ (ไม่สร้างใหม่แล้ว)
        Dorm dorm = null;
        if ("RESIDENT".equalsIgnoreCase(req.getRole())) {
            if (req.getDormId() == null) {
                throw new IllegalArgumentException("dormId is required for RESIDENT.");
            }
            dorm = dormRepository.findById(req.getDormId())
                    .orElseThrow(() -> new IllegalArgumentException("Dorm not found: " + req.getDormId()));
        }

        // 3) บันทึก Users ใน DB
        Users user = new Users();
        user.setFirebaseUid(userRecord.getUid());
        user.setEmail(req.getEmail());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRole(Users.Role.valueOf(req.getRole().toUpperCase()));
        user.setStatus(Users.Status.ACTIVE);
        user.setPosition(req.getPosition());     // ใช้เฉพาะ STAFF
        user.setRoomNumber(req.getRoomNumber()); // ใช้เฉพาะ RESIDENT
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (dorm != null) {
            user.setDorm(dorm); // ผูก FK
        }

        usersRepository.save(user);

        // 4) ตอบกลับ (แนะนำให้มี dormId ใน response)
        LoginResponse resp = new LoginResponse();
        resp.setUserId(user.getUserId());
        resp.setFirebaseUid(userRecord.getUid());
        resp.setEmail(req.getEmail());
        resp.setFirstName(req.getFirstName());
        resp.setLastName(req.getLastName());
        resp.setRole(req.getRole());
        resp.setPosition(req.getPosition());
        resp.setDormId(dorm != null ? dorm.getDormId() : null);   // ⬅️ ส่ง dormId
        // ถ้าอยากโชว์ชื่อด้วยก็เติมได้ (ไม่จำเป็นสำหรับ payload ฝั่ง FE)
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
