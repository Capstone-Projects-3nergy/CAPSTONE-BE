package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyParcelDto {
    private String residentName;
    private String trackingNumber;
}

