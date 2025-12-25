package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.CheckEmailRequest;
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
    public ResponseEntity<?> checkEmail(@RequestBody CheckEmailRequest req) {
        String email = req.getEmail().trim().toLowerCase();

        if (!usersRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email not registered in the system");
        }

        return ResponseEntity.ok(
                Map.of("message", "Email exists")
        );
    }
}
