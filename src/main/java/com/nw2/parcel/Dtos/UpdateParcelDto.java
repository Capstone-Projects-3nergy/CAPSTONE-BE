package com.nw2.parcel.Dtos;

import com.nw2.parcel.entity.Parcels;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateParcelDto {

    private String trackingNumber;
    private String recipientName;
    private Parcels.Parceltype parcelType;
    private String senderName;
    private Parcels.Status status;
    private Integer companyId;
    private String imageUrl;
    private Integer userId;
}

