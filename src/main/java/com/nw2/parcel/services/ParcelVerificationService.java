package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.VerifyParcelDto;
import com.nw2.parcel.entity.ParcelVerification;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.ParcelVerificationRepository;
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

    public void verifyParcel(VerifyParcelDto req) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String firebaseUid = auth.getName();

        Users resident = usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        ParcelVerification pv = new ParcelVerification();
        pv.setTrackingNumber(req.getTrackingNumber());
        pv.setResidentName(req.getResidentName());
        pv.setResident(resident);

        repo.save(pv);
    }
}

