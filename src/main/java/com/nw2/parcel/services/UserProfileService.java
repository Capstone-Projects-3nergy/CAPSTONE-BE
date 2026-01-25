package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.UpdateProfile;
import com.nw2.parcel.Dtos.UserProfileResponse;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.exception.UnauthorizedException;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UsersRepository usersRepository;
    private final FileStorageService fileStorageService; // ไว้เก็บรูป

    public UserProfileResponse updateProfile(
            String firebaseUid,
            UpdateProfile req,
            MultipartFile profileImage
    ) {

        Users user = usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        //update common fields
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setLineId(req.getLineId());

        //role specific
        if (user.getRole() == Users.Role.RESIDENT) {
            user.setRoomNumber(req.getRoomNumber());
        }

        if (user.getRole() == Users.Role.STAFF || user.getRole() == Users.Role.ADMIN) {
            user.setPosition(req.getPosition());
        }

        //profile image
        if (profileImage != null && !profileImage.isEmpty()) {

            // ลบรูปเก่า (ถ้ามี)
            if (user.getProfileImageUrl() != null) {
                fileStorageService.deleteFileByUrl(user.getProfileImageUrl());
            }

            // upload รูปใหม่
            String imageUrl = fileStorageService.uploadProfileImage(
                    profileImage,
                    user.getUserId()
            );

            user.setProfileImageUrl(imageUrl);
        }

        usersRepository.save(user);

        return mapToResponse(user);
    }

    private UserProfileResponse mapToResponse(Users user) {
        return UserProfileResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .lineId(user.getLineId())
                .roomNumber(user.getRoomNumber())
                .position(user.getPosition())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }

    public UserProfileResponse getProfile(String firebaseUid) {

        Users user = usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return mapToResponse(user);
    }
}

