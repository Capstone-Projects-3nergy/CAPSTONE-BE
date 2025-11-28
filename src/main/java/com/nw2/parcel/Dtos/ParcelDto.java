package com.nw2.parcel.Dtos;

import com.nw2.parcel.entity.Parcels;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelDto {
    private Integer parcelId;
    private String trackingNumber;
    private String recipientName;
    private Parcels.Status status;
    private Parcels.Parceltype parcelType;
    private String senderName;
    private Integer companyId;
    private String companyName;
    private Integer userId;

}