package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Integer userId;
    private String firebaseUid;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String position;
    private Integer dormId;
    private String dormName;
    private String roomNumber;
    private String message;
}