package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.ParcelVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParcelVerificationRepository
        extends JpaRepository<ParcelVerification, Integer> {

    Optional<ParcelVerification> findByTrackingNumberAndVerifiedFalse(String trackingNumber);
}