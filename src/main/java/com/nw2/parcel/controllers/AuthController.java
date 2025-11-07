package com.nw2.parcel.controllers;

import com.google.firebase.auth.FirebaseToken;
import com.nw2.parcel.Dtos.AuthVerifyDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.services.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {  "http://localhost:5173", "http://127.0.0.1:5173", "http://bscit.sit.kmutt.ac.th" }, allowedHeaders = {"Authorization", "Content-Type"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@RestController
@RequestMapping("/auth")
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
        FirebaseToken tok = (FirebaseToken) auth.getDetails();
        System.out.println("Verified ID token for uid: " );
        Users user = usersService.linkFirebaseOnLogin(tok);     // ครั้งแรกจะผูก uid ให้, ครั้งต่อๆ ไปจะผ่านเฉยๆ

        AuthVerifyDto body = new AuthVerifyDto(
                true,                           // authenticated
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getDorm() != null ? user.getDorm().getDormName() : null,
                user.getRoomNumber()
        );

        return ResponseEntity.ok(body);
    }

}