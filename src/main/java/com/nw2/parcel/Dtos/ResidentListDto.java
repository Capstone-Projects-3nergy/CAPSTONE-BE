package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResidentListDto {

    private Integer userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String roomNumber;
    private String email;
}
