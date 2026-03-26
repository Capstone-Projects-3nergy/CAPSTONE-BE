package com.nw2.parcel.Dtos;

import lombok.Data;

@Data
public class ManagementDetailDto {
    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String roomNumber;
    private String phoneNumber;
    private String lineId;
    private String profileImageUrl;
    private String role;
    private String status;
    private Integer dormId;
    private String dormName;
}