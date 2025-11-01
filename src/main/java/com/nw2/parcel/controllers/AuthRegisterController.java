package com.nw2.parcel.controllers;


import com.nw2.parcel.Dtos.RegisterDto;
import com.nw2.parcel.Dtos.UserDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.services.UsersService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:5173","http://192.168.103.151:5173"})

@RestController
@RequestMapping("/public/auth")  // endpoint สำหรับสมัคร (ไม่ต้อง auth)
public class AuthRegisterController {

    private final UsersService usersService;

    public AuthRegisterController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterDto req) {
        Users user = usersService.register(req);

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
                user.getDorm() != null ? user.getDorm().getDormName() : null,
                user.getRoomNumber(),
                user.getLineId(),
                user.getPosition()
        ));
    }
}
