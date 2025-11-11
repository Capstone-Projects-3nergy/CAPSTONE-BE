package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.SignUpRequest;
import com.nw2.parcel.Dtos.LoginResponse;
import com.nw2.parcel.services.FirebaseService;
import com.nw2.parcel.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://bscit.sit.kmutt.ac.th",
        "https://bscit.sit.kmutt.ac.th" })
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FirebaseService firebaseService;

    @PostMapping("/signup")
    public LoginResponse signup(@RequestBody SignUpRequest req) throws Exception {
        return userService.signUp(req);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestHeader("Authorization") String header) throws Exception {
        if (header == null || !header.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        String token = header.replace("Bearer ", "").trim();
        return userService.login(token, firebaseService);
    }


}
