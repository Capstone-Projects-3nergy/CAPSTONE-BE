package com.nw2.parcel.Dtos;

import lombok.Data;

@Data
public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String position;     // ใช้เฉพาะ staff
    private String dormName;     // ใช้เฉพาะ resident
    private String roomNumber;
    private String role;         // RESIDENT หรือ STAFF
}
