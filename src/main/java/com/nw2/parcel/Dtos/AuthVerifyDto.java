package com.nw2.parcel.Dtos;

public record AuthVerifyDto(
        boolean authenticated,  // โทเคนผ่านการยืนยันแล้ว
        long userId,
        String firebaseUid,
        String email,
        String firstName,
        String lastName,
        String role,
        String status,
        String dormName,
        String roomNumber,
        String profileImageUrl
) {}
