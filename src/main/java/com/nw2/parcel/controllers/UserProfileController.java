package com.nw2.parcel.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nw2.parcel.Dtos.UpdateProfile;
import com.nw2.parcel.Dtos.UserProfileResponse;
import com.nw2.parcel.services.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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

    @GetMapping
    public UserProfileResponse getProfile(Authentication authentication) {
        String firebaseUid = authentication.getName();
        return userProfileService.getProfile(firebaseUid);
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserProfileResponse updateProfile(
            @RequestPart("data") String data,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            Authentication authentication
    ) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        UpdateProfile request = mapper.readValue(data, UpdateProfile.class);

        String firebaseUid = authentication.getName();
        return userProfileService.updateProfile(firebaseUid, request, profileImage);
    }

}
