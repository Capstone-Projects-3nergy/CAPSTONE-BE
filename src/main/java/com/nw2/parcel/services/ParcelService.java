package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.CreateParcelDto;
import com.nw2.parcel.entity.Company;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.CompanyRepository;
import com.nw2.parcel.repositories.ParcelsRepository;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ParcelService {

    private final ParcelsRepository parcelsRepository;
    private final CompanyRepository companyRepository;
    private final UsersRepository usersRepository;

    public Parcels createParcel(CreateParcelDto req) {

        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found: " + req.getCompanyId()));

        Users user = usersRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));

        Parcels parcel = new Parcels();
        parcel.setTrackingNumber(req.getTrackingNumber());
        parcel.setRecipientName(req.getRecipientName());
        parcel.setParcelType(req.getParcelType());
        parcel.setSenderName(req.getSenderName());
        parcel.setStatus(Parcels.Status.PENDING);   // ตั้งค่า default
        parcel.setCompany(company);
        parcel.setUser(user);
        // receivedAt / updatedAt ให้ @PrePersist จัดการ

        return parcelsRepository.save(parcel);
    }
}
