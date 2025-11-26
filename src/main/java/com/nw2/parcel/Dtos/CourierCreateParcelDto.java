package com.nw2.parcel.Dtos;

import com.nw2.parcel.entity.Parcels;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourierCreateParcelDto {

    private String trackingNumber;            // เลขพัสดุ (ถ้ารู้)
    private String recipientName;            // ชื่อผู้รับที่เขียนบนกล่อง
    private Parcels.Parceltype parcelType;   // BOX / DOCUMENT / ELECTRONIC (เลือกจากหน้า UI)
    private String senderName;               // ชื่อผู้ส่ง
    private Integer companyId;               // บริษัทขนส่ง (Flash, Kerry ฯลฯ) ให้หน้า FE ส่ง id มา

    // ❌ ไม่มี userId – เพราะ courier ไม่รู้ว่าเป็น resident คนไหน
}