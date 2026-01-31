package com.nw2.parcel.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.nw2.parcel.Dtos.SignUpRequest;
import com.nw2.parcel.Dtos.LoginResponse;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.entity.Dorm;
import com.nw2.parcel.exception.EmailAlreadyExistsException;
import com.nw2.parcel.exception.UnauthorizedException;
import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.repositories.DormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UsersRepository usersRepository;
    private final DormRepository dormRepository;

    public LoginResponse signUp(SignUpRequest req) throws Exception {
        String normalizedEmail = req.getEmail().trim().toLowerCase();

        if (usersRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email is already in use.");
        }

        UserRecord userRecord;
        try {
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(normalizedEmail)
                    .setPassword(req.getPassword())
                    .setDisplayName(req.getFirstName() + " " + req.getLastName());

            userRecord = FirebaseAuth.getInstance().createUser(createRequest);

        } catch (FirebaseAuthException e) {
            if ("EMAIL_ALREADY_EXISTS".equals(e.getErrorCode())) {
                throw new EmailAlreadyExistsException("Email is already in use.");
            }
            throw e;
        }

        Dorm dorm = null;
        if ("RESIDENT".equalsIgnoreCase(req.getRole())) {
            if (req.getDormId() == null) {
                throw new IllegalArgumentException("dormId is required for RESIDENT.");
            }
            dorm = dormRepository.findById(req.getDormId())
                    .orElseThrow(() -> new IllegalArgumentException("Dorm not found: " + req.getDormId()));
        }

        Users user = new Users();
        user.setFirebaseUid(userRecord.getUid());
        user.setEmail(normalizedEmail);
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRole(Users.Role.valueOf(req.getRole().toUpperCase()));
        user.setStatus(Users.Status.PENDING);
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

    public LoginResponse login(String idToken, FirebaseService firebaseService) {
        try {
            var decoded = firebaseService.verifyIdToken(idToken);
            final String uid = decoded.getUid();
            final String emailFromToken = decoded.getEmail();

            if (!decoded.isEmailVerified()) {
                throw new UnauthorizedException("Email not verified");
            }

            Users u = usersRepository.findByFirebaseUid(uid)
                    .orElseThrow(() -> new UnauthorizedException("Please register before login"));

            // ❌ ห้าม throw ถ้าไม่ ACTIVE
            // ✅ ให้ login แล้วเปลี่ยนเป็น ACTIVE
            if (u.getStatus() == Users.Status.PENDING
                    || u.getStatus() == Users.Status.INACTIVE) {

                u.setStatus(Users.Status.ACTIVE);
            }

            if (u.getStatus() == Users.Status.DELETED) {
                throw new UnauthorizedException("Account has been deleted");
            }

            u.setUpdatedAt(LocalDateTime.now());
            usersRepository.save(u);

            LoginResponse resp = new LoginResponse();
            resp.setUserId(u.getUserId());
            resp.setFirebaseUid(uid);
            resp.setEmail(u.getEmail() != null ? u.getEmail() : emailFromToken);
            resp.setFirstName(u.getFirstName());
            resp.setLastName(u.getLastName());
            resp.setRole(u.getRole().name());
            resp.setPosition(u.getPosition());

            if (u.getDorm() != null) {
                resp.setDormId(u.getDorm().getDormId());
                resp.setDormName(u.getDorm().getDormName());
            }

            resp.setRoomNumber(u.getRoomNumber());
            resp.setMessage("Login successful");
            return resp;

        } catch (FirebaseAuthException e) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }


    @Transactional
    public void logout(String idToken, FirebaseService firebaseService) {
        try {
            var decoded = firebaseService.verifyIdToken(idToken);
            String uid = decoded.getUid();

            Users user = usersRepository.findByFirebaseUid(uid)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));

            user.setStatus(Users.Status.INACTIVE);
            user.setUpdatedAt(LocalDateTime.now());

            usersRepository.save(user);

        } catch (FirebaseAuthException e) {
            throw new UnauthorizedException("Invalid or expired token");
        }
    }
}
