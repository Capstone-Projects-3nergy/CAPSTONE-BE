package com.nw2.parcel.controllers;

import com.google.firebase.auth.FirebaseToken;
import com.nw2.parcel.Dtos.AuthVerifyDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.services.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:5173","http://192.168.103.151:5173"})

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsersService usersService;

    public AuthController(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * ยืนยันว่า ID token ถูกต้อง (ผ่าน FirebaseAuthFilter แล้ว)
     * และ "ผูก firebaseUid" ให้ user ที่สมัครไว้ ถ้ายังไม่เคยผูก (ครั้งแรก)
     * ใช้สำหรับเปิดแอป/รีเฟรชหน้า แล้วเช็คว่าผู้ใช้ล็อกอินอยู่จริง
     */
    @GetMapping("/verify")
    public ResponseEntity<AuthVerifyDto> verify(Authentication auth) {
        FirebaseToken tok = (FirebaseToken) auth.getDetails(); // ใส่มาโดย FirebaseAuthFilter
        Users user = usersService.linkFirebaseOnLogin(tok);     // ครั้งแรกจะผูก uid ให้, ครั้งต่อ ๆ ไปจะผ่านเฉย ๆ

        AuthVerifyDto body = new AuthVerifyDto(
                true,                           // authenticated
                user.getUserId(),
                user.getFirebaseUid(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()   != null ? user.getRole().name()   : null,
                user.getStatus() != null ? user.getStatus().name() : null,
                user.getDorm()   != null ? user.getDorm().getDormName() : null,
                user.getRoomNumber(),
                user.getProfileImageUrl()
        );
        return ResponseEntity.ok(body);
    }
}