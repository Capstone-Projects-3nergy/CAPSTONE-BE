package com.nw2.parcel.controllers;

import com.google.firebase.auth.FirebaseAuth;
import com.nw2.parcel.Dtos.CheckEmailRequest;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2"
})
@RestController
@RequestMapping("/api/public/email")
@RequiredArgsConstructor
public class EmailController {

    private final UsersRepository usersRepository;

    @PostMapping("/check")
    public ResponseEntity<?> checkEmail(@RequestBody CheckEmailRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        boolean canReset = true;
        try {
            Users user = usersRepository.findByEmail(email)
                    .orElseThrow();
            FirebaseAuth.getInstance().getUserByEmail(email);

        } catch (Exception e) {
            canReset = false;
            System.out.println("[RESET-PASSWORD] email check failed: " + email);
        }
        return ResponseEntity.ok(Map.of(
                "message", "If the email exists, a password reset link will be sent."
        ));
    }
}
