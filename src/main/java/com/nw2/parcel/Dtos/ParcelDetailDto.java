package com.nw2.parcel.Dtos;

import com.nw2.parcel.entity.Parcels;
import java.time.LocalDateTime;

public record ParcelDetailDto(
        Integer parcelId,
        String trackingNumber,
        Parcels.Status status,
        String parcelType,
        String senderName,
        String imageUrl,
        LocalDateTime receivedAt,
        LocalDateTime pickedUpAt,

        // บริษัท
        Integer companyId,
        String companyName,

        // ผู้พัก (แสดงบนหน้าแก้ไข)
        Integer residentUserId,
        String residentName,
        String phone,
        String roomNumber,
        String dormName
) {}
