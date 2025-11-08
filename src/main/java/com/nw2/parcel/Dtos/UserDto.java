package com.nw2.parcel.Dtos;

public record UserDto(
        Integer userId,
        String firebaseUid,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String profileImageUrl,
        String role,
        String status,
        String dormName,
        String roomNumber,
        String lineId,
        String position
) {}