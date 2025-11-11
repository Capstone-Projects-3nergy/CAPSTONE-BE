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
        // 1️⃣ สร้าง user ใน Firebase
        UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                .setEmail(req.getEmail())
                .setPassword(req.getPassword())
                .setDisplayName(req.getFirstName() + " " + req.getLastName());

        UserRecord userRecord = FirebaseAuth.getInstance().createUser(createRequest);

        // 2️⃣ ตรวจว่ามี Dorm นี้อยู่หรือยัง (เฉพาะ role RESIDENT)
        Dorm dorm = null;
        if (req.getRole().equalsIgnoreCase("RESIDENT") && req.getDormName() != null) {
            dorm = dormRepository.findByDormName(req.getDormName())
                    .orElseGet(() -> {
                        Dorm newDorm = new Dorm();
                        newDorm.setDormName(req.getDormName());
                        newDorm.setAddress("N/A");
                        newDorm.setDormType(Dorm.DormType.Male_Dormitory); // default
                        newDorm.setCreatedAt(LocalDateTime.now());
                        newDorm.setUpdatedAt(LocalDateTime.now());
                        return dormRepository.save(newDorm);
                    });
        }

        // 3️⃣ สร้าง user ใน DB
        Users user = new Users();
        user.setFirebaseUid(userRecord.getUid());
        user.setEmail(req.getEmail());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRole(Users.Role.valueOf(req.getRole().toUpperCase()));
        user.setStatus(Users.Status.ACTIVE);
        user.setPosition(req.getPosition());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (dorm != null) user.setDorm(dorm);

        usersRepository.save(user);

        return new LoginResponse(userRecord.getUid(), req.getEmail(), "Signup successful");
    }

    public LoginResponse login(String idToken, FirebaseService firebaseService) throws Exception {
        var decoded = firebaseService.verifyIdToken(idToken);
        return new LoginResponse(decoded.getUid(), decoded.getEmail(), "Login successful");
    }
}