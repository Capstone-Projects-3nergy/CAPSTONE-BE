package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.RegisterDto;
import com.nw2.parcel.Dtos.UserDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.services.UsersService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = { "*", "http://localhost:5173", "http://127.0.0.1:5173" })

@RestController
@RequestMapping("/auth")  // endpoint สำหรับสมัคร (ไม่ต้อง auth)
public class AuthRegisterController {

    private final UsersService usersService;

    public AuthRegisterController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Validated @RequestBody RegisterDto req) {
        Users user = usersService.register(req);

        // กัน null เวลาผู้ใช้เป็น STAFF (ไม่มี dorm)
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
                dormName,                 // ใช้ค่าที่กัน null แล้ว
                user.getRoomNumber(),
                user.getLineId(),
                user.getPosition()
        ));
    }

}

