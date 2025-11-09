package com.nw2.parcel.Dtos;

public record AuthVerifyDto(
        boolean authenticated,  // token ผ่านการยืนยันแล้ว
        Integer userId,
        String email,
        String firstName,
        String lastName,
        String role,
        String dormName,
        String roomNumber,
        String position
) {}
