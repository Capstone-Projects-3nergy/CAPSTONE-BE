package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ManagementListDto {
    private Integer userId;
    private String fullName;
    private String email;
    private String roomNumber;
    private String profileImageUrl;
    private String role;
    private String dormName;
    private String status;
    private LocalDateTime updatedAt;
}
