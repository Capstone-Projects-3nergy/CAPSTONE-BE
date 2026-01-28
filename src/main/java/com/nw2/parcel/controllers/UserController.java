package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.SignUpRequest;
import com.nw2.parcel.Dtos.LoginResponse;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.services.FirebaseService;
import com.nw2.parcel.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://cp25nw2.sit.kmutt.ac.th",
        "https://bscit.sit.kmutt.ac.th"
})
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FirebaseService firebaseService;
    private final UsersRepository usersRepository;

    @PostMapping("/signup")
    public LoginResponse signup(@RequestBody SignUpRequest req) throws Exception {
        return userService.signUp(req);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestHeader("Authorization") String header) throws Exception {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        String token = header.substring(7).trim();
        LoginResponse resp = userService.login(token, firebaseService);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = header.substring(7).trim();
        userService.logout(token, firebaseService);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activateAccount(Authentication auth) {
        String firebaseUid = auth.getName();

        Users user = usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (user.getStatus() != Users.Status.ACTIVE) {
            user.setStatus(Users.Status.ACTIVE);
            user.setUpdatedAt(LocalDateTime.now());
            usersRepository.save(user);
        }

        return ResponseEntity.ok().build();
    }

}
