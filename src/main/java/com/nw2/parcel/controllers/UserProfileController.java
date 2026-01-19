package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.UpdateProfile;
import com.nw2.parcel.Dtos.UserProfileResponse;
import com.nw2.parcel.services.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://cp25nw2.sit.kmutt.ac.th",
        "https://cp25nw2.sit.kmutt.ac.th"
})

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PutMapping(consumes = "multipart/form-data")
    public UserProfileResponse updateProfile(
            @RequestPart("data") UpdateProfile request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            Authentication authentication
    ) {
        String firebaseUid = authentication.getName();
        return userProfileService.updateProfile(firebaseUid, request, profileImage);
    }
}
