package com.nw2.parcel.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.nw2.parcel.Dtos.CheckEmailRequest;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/email")
@RequiredArgsConstructor
public class EmailController {

    private final UsersRepository usersRepository;

    @PostMapping("/check")
//    public ResponseEntity<?> checkEmail(@RequestBody CheckEmailRequest req) {
//        String email = req.getEmail().trim().toLowerCase();
//
//        if (!usersRepository.existsByEmail(email)) {
//            throw new IllegalArgumentException("Email not registered in the system");
//        }
//
//        return ResponseEntity.ok(
//                Map.of("message", "Email exists")
//        );
//    }
    public ResponseEntity<?> checkEmail(@RequestBody CheckEmailRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        boolean canReset = true;

        try {
            // 1) เช็คใน DB
            Users user = usersRepository.findByEmail(email)
                    .orElseThrow();

            // 2) เช็คใน Firebase
            FirebaseAuth.getInstance().getUserByEmail(email);

        } catch (Exception e) {
            // ❗ ห้ามส่ง error จริงกลับไป
            canReset = false;

            // log เพื่อ debug ฝั่ง backend
            System.out.println("[RESET-PASSWORD] email check failed: " + email);
        }

        // 3) ตอบเหมือนกันทุกกรณี
        return ResponseEntity.ok(Map.of(
                "message", "If the email exists, a password reset link will be sent."
        ));
    }
}
