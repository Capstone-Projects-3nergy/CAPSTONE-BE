package com.nw2.parcel.Dtos;

import com.nw2.parcel.entity.Parcels;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParcelListItemDto {
    private Integer parcelId;
    private String trackingNumber;
    private String ownerName;
    private String roomNumber;
    private String contactEmail;
    private Parcels.Status status;
    private LocalDateTime receivedAt;
    private LocalDateTime updatedAt;
}
