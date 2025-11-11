package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor     // ⬅️ เพิ่มบรรทัดนี้
@AllArgsConstructor
public class LoginResponse {
    private Integer userId;
    private String firebaseUid;
    private String email;
    private String firstName;
    private String lastName;
    private String role;        // RESIDENT | STAFF | ADMIN
    private String position;    // STAFF เท่านั้น (อาจเป็น null)
    private String dormName;    // RESIDENT เท่านั้น (อาจเป็น null)
    private String roomNumber;  // RESIDENT เท่านั้น (อาจเป็น null)
    private String message;
}