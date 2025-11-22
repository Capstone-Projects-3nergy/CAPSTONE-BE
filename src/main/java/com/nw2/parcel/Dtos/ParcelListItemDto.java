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

    // ชื่อเจ้าของพัสดุ (resident)
    private String ownerName;      // ex. "Pimpajee Sxxxxxx"

    private String roomNumber;     // จาก Users.roomNumber

    private String contactEmail;   // email ของเจ้าของพัสดุ

    private Parcels.Status status; // PENDING / RECEIVED / PICKED_UP

    private LocalDateTime receivedAt; // เวลา receive ไว้โชว์ในตาราง
}
