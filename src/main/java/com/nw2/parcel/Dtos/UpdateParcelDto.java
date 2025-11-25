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
    private Parcels.Status status;   //  RECEIVED / PICKED_UP
    private Integer companyId;       // เปลี่ยนขนส่งได้
    private String imageUrl;         // เปลี่ยนรูป (เช่น URL ใหม่)
}
//ทุกฟิลด์ใน DTO นี้คือ "ของที่สตาฟมีสิทธิ์แก้"
