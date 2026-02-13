package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.VerifyParcelDto;
import com.nw2.parcel.entity.ParcelVerification;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.exception.UnauthorizedException;
import com.nw2.parcel.repositories.ParcelVerificationRepository;
import com.nw2.parcel.repositories.ParcelsRepository;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParcelVerificationService {

    private final ParcelVerificationRepository repo;
    private final UsersRepository usersRepository;
    private final ParcelsRepository parcelsRepository;
    private final NotificationService notificationService;

    public void verifyParcel(VerifyParcelDto req) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String firebaseUid = auth.getName();

        Users resident = usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // ✅ normalize tracking number
        String normalizedTracking = req.getTrackingNumber()
                .trim()
                .toUpperCase();

        ParcelVerification pv = new ParcelVerification();
        pv.setTrackingNumber(normalizedTracking);
        pv.setResidentName(req.getResidentName());
        pv.setResident(resident);
        pv.setVerified(false);

        repo.save(pv);

        // 🔥 ถ้า parcel มีอยู่แล้ว → assign ทันที
        parcelsRepository
                .findByTrackingNumberIgnoreCase(normalizedTracking)
                .ifPresent(parcel -> {

                    parcel.setUser(resident);
                    pv.setVerified(true);

                    parcelsRepository.save(parcel);
                    repo.save(pv);

                    notificationService
                            .notifyParcelMultiChannel(parcel, resident);
                });
    }
}

