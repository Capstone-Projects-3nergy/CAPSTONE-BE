package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrashResidentDto {

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
    private LocalDateTime deletedAt;
    private String deletedBy;
}