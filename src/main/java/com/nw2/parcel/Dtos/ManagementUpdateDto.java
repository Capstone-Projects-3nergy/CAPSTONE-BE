package com.nw2.parcel.Dtos;

import lombok.Data;

@Data
public class ManagementUpdateDto {
    private String firstName;
    private String lastName;
    private String roomNumber;
    private String phoneNumber;
    private String lineId;
    private Integer dormId;
}
