// com.nw2.parcel.controllers.AuthController
package com.nw2.parcel.controllers;

import com.google.firebase.auth.FirebaseToken;
import com.nw2.parcel.Dtos.AuthVerifyDto;
import com.nw2.parcel.Dtos.RegisterDto;
import com.nw2.parcel.Dtos.UserDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.services.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")   // << ขยับมาใต้ /api
@CrossOrigin(origins = { "http://localhost:5173", "http://127.0.0.1:5173", "http://bscit.sit.kmutt.ac.th","https://bscit.sit.kmutt.ac.th" },
        allowedHeaders = {"Authorization","Content-Type"},
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class AuthController {
    private final UsersService usersService;
    public AuthController(UsersService usersService) { this.usersService = usersService; }

    /** GET /api/auth/verify (ต้องแนบ Firebase ID token) */
    @GetMapping("/verify")
    public ResponseEntity<AuthVerifyDto> verify(Authentication auth) {
        FirebaseToken tok = (FirebaseToken) auth.getDetails();
        Users user = usersService.linkFirebaseOnLogin(tok);

        var body = new AuthVerifyDto(
                true,
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getDorm() != null ? user.getDorm().getDormName() : null,
                user.getRoomNumber(),
                user.getPosition()
        );
        return ResponseEntity.ok(body);
    }
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Validated @RequestBody RegisterDto req) {
        Users user = usersService.register(req);
        String dormName = (user.getDorm() != null) ? user.getDorm().getDormName() : null;

        return ResponseEntity.ok(new UserDto(
                user.getUserId(),
                user.getFirebaseUid(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getProfileImageUrl(),
                user.getRole().name(),
                user.getStatus().name(),
                dormName,
                user.getRoomNumber(),
                user.getLineId(),
                user.getPosition()
        ));
    }
}
