package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Parcels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelsRepository extends JpaRepository<Parcels, Integer> {
    List<Parcels> findAllByOrderByReceivedAtDesc();
    // staff ใช้ดูหรือแก้ detail ทีละอัน (ไม่เอาถังขยะ)
    Optional<Parcels> findByParcelIdAndIsDeletedFalse(Integer parcelId);

    // ถ้าภายหลังอยากทำหน้า “ถังขยะ” ก็ใช้เมธอดนี้ได้
    List<Parcels> findAllByIsDeletedTrueOrderByDeletedAtDesc();
    // (2) resident → ดูเฉพาะพัสดุของตัวเอง
    List<Parcels> findByUserUserIdOrderByReceivedAtDesc(Integer userId);

    // (3) resident → ใช้ตอนดูรายละเอียดหรือ confirm แต่ต้องเป็นของตัวเอง
    Optional<Parcels> findByParcelIdAndUserUserId(Integer parcelId, Integer userId);

}