package com.nw2.parcel.Dtos;

import com.nw2.parcel.entity.Parcels;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateParcelDto {
    private String trackingNumber;
    private String recipientName;
    private Parcels.Parceltype parcelType;
    private String senderName;
    private Integer companyId;
    private Integer userId;

}
