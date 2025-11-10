package com.nw2.parcel.Dtos;

import com.nw2.parcel.entity.Parcels;
import java.time.LocalDateTime;

public record EditParcelRequest(
        Integer parcelId,          // จะ override ด้วย path variable ให้ตรง
        Parcels.Status status,     // PENDING / RECEIVED / PICKED_UP
        LocalDateTime pickedUpAt,  // ถ้า PICKED_UP แล้วไม่ส่ง จะตั้งเป็น now()
        String parcelType,
        String senderName,
        String imageUrl,
        Integer companyId,         // แก้บริษัทได้
        String companyName         // หรือส่งชื่อใหม่ให้สร้างอัตโนมัติ
) {}
