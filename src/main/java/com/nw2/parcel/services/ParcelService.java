package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.CreateParcelDto;
import com.nw2.parcel.Dtos.ParcelListItemDto;
import com.nw2.parcel.entity.Company;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.CompanyRepository;
import com.nw2.parcel.repositories.ParcelsRepository;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParcelService {

    private final ParcelsRepository parcelsRepository;
    private final CompanyRepository companyRepository;
    private final UsersRepository usersRepository;

    public Parcels createParcel(CreateParcelDto req) {

        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found: " + req.getCompanyId()));

//        Users user = usersRepository.findById(req.getUserId())
//                .orElseThrow(() -> new RuntimeException("User not found: " + req.getUserId()));
        Users resident = usersRepository
                .findByUserIdAndRole(req.getUserId(), Users.Role.RESIDENT)
                .orElseThrow(() -> new RuntimeException(
                        "Resident not found with id: " + req.getUserId()
                ));

        Parcels parcel = new Parcels();
        parcel.setTrackingNumber(req.getTrackingNumber());
        parcel.setRecipientName(req.getRecipientName());
        parcel.setParcelType(req.getParcelType());
        parcel.setSenderName(req.getSenderName());
        parcel.setStatus(Parcels.Status.PENDING);   // ตั้งค่า default
        parcel.setCompany(company);
//        parcel.setUser(user);
        parcel.setUser(resident);
        // receivedAt / updatedAt ให้ @PrePersist จัดการ

        return parcelsRepository.save(parcel);
    }

    public List<ParcelListItemDto> getAllParcelsForStaff() {
        List<Parcels> parcels = parcelsRepository.findAllByOrderByReceivedAtDesc();

        return parcels.stream()
                .map(p -> {
                    // ถ้ามี user (RESIDENT) ผูกอยู่
                    String ownerName = null;
                    String roomNumber = null;
                    String contactEmail = null;

                    if (p.getUser() != null) {
                        ownerName = (p.getUser().getFirstName() != null ? p.getUser().getFirstName() : "")
                                + (p.getUser().getLastName() != null ? " " + p.getUser().getLastName() : "");
                        roomNumber = p.getUser().getRoomNumber();
                        contactEmail = p.getUser().getEmail();
                    } else {
                        // fallback ใช้ recipientName ถ้าไม่มี Users
                        ownerName = p.getRecipientName();
                    }

                    return new ParcelListItemDto(
                            p.getParcelId(),
                            p.getTrackingNumber(),
                            ownerName,
                            roomNumber,
                            contactEmail,
                            p.getStatus(),
                            p.getReceivedAt()
                    );
                })
                .collect(Collectors.toList());
    }
}
