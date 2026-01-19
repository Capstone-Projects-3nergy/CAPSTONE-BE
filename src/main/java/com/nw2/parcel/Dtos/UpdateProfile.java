package com.nw2.parcel.Dtos;

import lombok.Data;

@Data
public class UpdateProfile {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String lineId;

    // resident only
    private String roomNumber;

    // staff only
    private String position;
}