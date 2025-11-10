package com.nw2.parcel.Dtos;

//public record AddParcelRequest(
//        String trackingNumber,
//        String recipientName,      // ชื่อผู้รับที่จะพิมพ์บนใบ (ถ้าไม่กรอก เราจะ fallback จาก resident)
//        Integer companyId,
//        String companyName,
//        Integer residentUserId,
//        String residentFirstName,
//        String residentLastName,
//        String residentPhone,
//        String dormName,
//        String roomNumber,
//        String parcelType,
//        String senderName,
//        String imageUrl
//) {}
public record AddParcelRequest(
        String trackingNumber,
        Integer companyId,
        String companyName,   // เผื่อบริษัทใหม่
        String recipientName,
        Integer residentUserId, // หรือจะส่ง dorm+room ก็ได้ (แล้ว backend หาผู้พัก)
        String parcelType,
        String senderName,
        String imageUrl
) {}