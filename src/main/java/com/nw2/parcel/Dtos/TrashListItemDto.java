package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrashListItemDto {
    private Integer parcelId;
    private String trackingNumber;
    private String ownerName;
    private String roomNumber;
    private String contactEmail;
    private LocalDateTime deletedAt;
    private String deletedByName;
}