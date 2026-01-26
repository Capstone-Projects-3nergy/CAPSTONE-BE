package com.nw2.parcel.Dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {
    private Integer userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String lineId;
    private String roomNumber;
    private String position;
    private String profileImageUrl;
    private String role;
    private String status;
}