package com.nw2.parcel.Dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateParcelDto {
    private String trackingNumber;
    private String recipientName;
    private String parcelType;
    private String senderName;

    private Integer companyId;  // FK ไป Company
    private Integer userId;     // FK ไป Users (เจ้าของพัสดุ = resident)

    // ถ้าอยากเก็บเพิ่ม เช่น roomNumber/contact ไว้ใช้ทีหลัง
    // private String roomNumber;
    // private String contact;
}
