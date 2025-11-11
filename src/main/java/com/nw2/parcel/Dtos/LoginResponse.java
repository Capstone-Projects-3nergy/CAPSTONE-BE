package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String FirebaseUid;
    private String email;
    private String message;
}