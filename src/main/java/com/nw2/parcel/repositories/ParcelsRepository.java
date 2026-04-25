package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Parcels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelsRepository extends JpaRepository<Parcels, Integer> {

    Optional<Parcels> findByParcelIdAndIsDeletedFalse(Integer parcelId);

    List<Parcels> findAllByIsDeletedTrueOrderByDeletedAtDesc();

    List<Parcels> findAllByIsDeletedFalseOrderByReceivedAtDesc();

    Optional<Parcels> findByParcelIdAndUserUserIdAndIsDeletedFalse(Integer parcelId, Integer userId);

    Optional<Parcels> findByTrackingNumberIgnoreCase(String trackingNumber);

    // เดิม — ยังใช้ได้อยู่ (ใช้ใน OverdueService ถ้ายัง query status เดียว)
    List<Parcels> findByStatusAndIsDeletedFalse(Parcels.Status status);

    // เพิ่มใหม่ — รองรับ query หลาย status พร้อมกัน (WAITING + OVERDUE)
    List<Parcels> findByStatusInAndIsDeletedFalse(List<Parcels.Status> statuses);

    @Query("""
        SELECT p FROM Parcels p
        WHERE p.user.userId = :userId
        AND p.isDeleted = false
        AND EXISTS (
            SELECT pv FROM ParcelVerification pv
            WHERE UPPER(pv.trackingNumber) = UPPER(p.trackingNumber)
            AND pv.resident.userId = :userId
            AND pv.verified = true
        )
        ORDER BY p.receivedAt DESC
    """)
    List<Parcels> findMatchedParcelsByUserId(@Param("userId") Integer userId);
}
