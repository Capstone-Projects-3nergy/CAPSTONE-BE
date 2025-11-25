package com.nw2.parcel.Dtos;

import com.nw2.parcel.entity.Parcels;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelDetailDto {
    private Integer parcelId;
    private String trackingNumber;
    private String recipientName;
    private Parcels.Status status;
    private Parcels.Parceltype parcelType;
    private String senderName;
    private String imageUrl;
    private LocalDateTime receivedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime updatedAt;
    private Integer companyId;
    private String companyName;
    private Integer residentId;
    private String residentName;
    private String roomNumber;
    private String email;
}

