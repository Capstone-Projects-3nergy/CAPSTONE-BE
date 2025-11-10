package com.nw2.parcel.Dtos;

import java.time.LocalDateTime;

public record ParcelListItemDto(
        Integer parcelId,
        String trackingNumber,
        String companyName,
        String residentName,
        String roomNumber,
        String contact,           // phone
        String status,            // PENDING / RECEIVED / PICKED_UP
        LocalDateTime receivedAt
) {}
