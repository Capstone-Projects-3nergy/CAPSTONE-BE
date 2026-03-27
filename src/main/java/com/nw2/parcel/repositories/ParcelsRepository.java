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
    List<Parcels> findAllByOrderByReceivedAtDesc();
    Optional<Parcels> findByParcelIdAndIsDeletedFalse(Integer parcelId);
    List<Parcels> findAllByIsDeletedTrueOrderByDeletedAtDesc();
    List<Parcels> findAllByIsDeletedFalseOrderByReceivedAtDesc();
    List<Parcels> findByUserUserIdOrderByReceivedAtDesc(Integer userId);
    List<Parcels> findByUserUserIdAndIsDeletedFalseOrderByReceivedAtDesc(Integer userId);
    Optional<Parcels> findByParcelIdAndUserUserIdAndIsDeletedFalse(Integer parcelId, Integer userId);
    Optional<Parcels> findByParcelIdAndUserUserId(Integer parcelId, Integer userId);
    Optional<Parcels> findByParcelIdAndIsDeletedTrue(Integer parcelId);
    Optional<Parcels> findByTrackingNumberIgnoreCase(String trackingNumber);
    List<Parcels> findByStatusAndIsDeletedFalse(Parcels.Status status);
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