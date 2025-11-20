package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.*;
import com.nw2.parcel.entity.Company;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.exception.ParcelNotFoundException;
import com.nw2.parcel.repositories.CompanyRepository;
import com.nw2.parcel.repositories.ParcelsRepository;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParcelService {

    private static final Logger log = LoggerFactory.getLogger(ParcelService.class);

    private final ParcelsRepository parcelsRepository;
    private final CompanyRepository companyRepository;
    private final UsersRepository usersRepository;

    // add
    public Parcels createParcel(CreateParcelDto req) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found: " + req.getCompanyId())
                );

        Users resident = usersRepository
                .findByUserIdAndRole(req.getUserId(), Users.Role.RESIDENT)
                .orElseThrow(() ->
                        new IllegalArgumentException("Resident not found with id: " + req.getUserId())
                );

        Parcels parcel = new Parcels();
        parcel.setTrackingNumber(req.getTrackingNumber());
        parcel.setRecipientName(req.getRecipientName());
        parcel.setParcelType(req.getParcelType());
        parcel.setSenderName(req.getSenderName());
        parcel.setStatus(Parcels.Status.PENDING);   // default
        parcel.setCompany(company);
        parcel.setUser(resident);

        return parcelsRepository.save(parcel);
    }

    // view
    public List<ParcelListItemDto> getAllParcelsForStaff() {
        List<Parcels> parcels = parcelsRepository.findAllByOrderByReceivedAtDesc();

        return parcels.stream()
                .map(p -> {
                    String ownerName = null;
                    String roomNumber = null;
                    String contactEmail = null;

                    if (p.getUser() != null) {
                        ownerName = (p.getUser().getFirstName() != null ? p.getUser().getFirstName() : "")
                                + (p.getUser().getLastName() != null ? " " + p.getUser().getLastName() : "");
                        roomNumber = p.getUser().getRoomNumber();
                        contactEmail = p.getUser().getEmail();
                    } else {
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

    // details
    public ParcelDetailDto getParcelDetail(Integer parcelId) {
        Parcels p = parcelsRepository.findById(parcelId)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        // company info
        Integer companyId = null;
        String companyName = null;
        if (p.getCompany() != null) {
            companyId = p.getCompany().getCompanyId();
            companyName = p.getCompany().getCompanyName();
        }

        // resident/user info
        Integer residentId = null;
        String residentName = null;
        String roomNumber = null;
        String email = null;
        if (p.getUser() != null) {
            residentId = p.getUser().getUserId();
            String firstName = p.getUser().getFirstName();
            String lastName = p.getUser().getLastName();
            residentName =
                    (firstName != null ? firstName : "") +
                            (lastName != null ? " " + lastName : "");
            roomNumber = p.getUser().getRoomNumber();
            email = p.getUser().getEmail();
        }

        return new ParcelDetailDto(
                p.getParcelId(),
                p.getTrackingNumber(),
                p.getRecipientName(),
                p.getStatus(),
                p.getParcelType(),
                p.getSenderName(),
                p.getImageUrl(),
                p.getReceivedAt(),
                p.getPickedUpAt(),
                p.getUpdatedAt(),
                companyId,
                companyName,
                residentId,
                residentName,
                roomNumber,
                email
        );
    }

    // ✏️ edit parcel + status สำหรับ STAFF
    public ParcelDetailDto updateParcelForStaff(Integer parcelId, UpdateParcelDto req) {
        Parcels p = parcelsRepository.findById(parcelId)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        // ---------- ฟิลด์ทั่วไปที่อนุญาตให้แก้ ----------
        if (req.getTrackingNumber() != null) {
            p.setTrackingNumber(req.getTrackingNumber());
        }

        if (req.getRecipientName() != null) {
            p.setRecipientName(req.getRecipientName());
        }

        if (req.getParcelType() != null) {
            p.setParcelType(req.getParcelType());
        }

        if (req.getSenderName() != null) {
            p.setSenderName(req.getSenderName());
        }

        if (req.getImageUrl() != null) {
            p.setImageUrl(req.getImageUrl());
        }

        if (req.getCompanyId() != null
                && (p.getCompany() == null
                || !req.getCompanyId().equals(p.getCompany().getCompanyId()))) {

            Company company = companyRepository.findById(req.getCompanyId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Company not found: " + req.getCompanyId())
                    );
            p.setCompany(company);
        }

        // ---------- ส่วนของ status + กฎ transition ----------
        Parcels.Status oldStatus = p.getStatus();
        Parcels.Status newStatus = req.getStatus();

        // เช็กกฎก่อน ถ้าผิดจะ throw 400 ทันที
        validateStatusTransition(oldStatus, newStatus);

        if (newStatus != null && newStatus != oldStatus) {
            p.setStatus(newStatus);

            // ถ้า transition คือ RECEIVED -> PICKED_UP (เพิ่งเปลี่ยนเป็น PICKED_UP ครั้งแรก)
            if (newStatus == Parcels.Status.PICKED_UP && p.getPickedUpAt() == null) {
                p.setPickedUpAt(java.time.LocalDateTime.now());
            }

            // ถ้าอยาก strict กว่านี้ เช่น:
            // - ไม่ให้ย้อน RECEIVED -> PENDING
            // - ไม่ให้แก้เวลา pickedUpAt ย้อนหลัง
            // สามารถขยาย logic ตรงนี้เพิ่มได้
        }

        // save -> จะไปเข้า @PreUpdate แล้ว updatedAt = now ให้อัตโนมัติ
        Parcels updated = parcelsRepository.save(p);

        // ---------- map กลับเป็น ParcelDetailDto (เหมือน getParcelDetail) ----------
        Integer companyId = null;
        String companyName = null;
        if (updated.getCompany() != null) {
            companyId = updated.getCompany().getCompanyId();
            companyName = updated.getCompany().getCompanyName();
        }

        Integer residentId = null;
        String residentName = null;
        String roomNumber = null;
        String email = null;
        if (updated.getUser() != null) {
            residentId = updated.getUser().getUserId();
            String firstName = updated.getUser().getFirstName();
            String lastName = updated.getUser().getLastName();
            residentName =
                    (firstName != null ? firstName : "") +
                            (lastName != null ? " " + lastName : "");
            roomNumber = updated.getUser().getRoomNumber();
            email = updated.getUser().getEmail();
        }

        return new ParcelDetailDto(
                updated.getParcelId(),
                updated.getTrackingNumber(),
                updated.getRecipientName(),
                updated.getStatus(),
                updated.getParcelType(),
                updated.getSenderName(),
                updated.getImageUrl(),
                updated.getReceivedAt(),
                updated.getPickedUpAt(),
                updated.getUpdatedAt(),
                companyId,
                companyName,
                residentId,
                residentName,
                roomNumber,
                email
        );
    }

    // เช็ก transition ระหว่าง oldStatus -> newStatus
    private void validateStatusTransition(Parcels.Status oldStatus, Parcels.Status newStatus) {
        if (newStatus == null || oldStatus == newStatus) {
            // ไม่เปลี่ยน หรือ ไม่ส่ง status มาเลย -> ok
            return;
        }
        switch (oldStatus) {
            case PENDING:
                // จาก PENDING -> อนุญาตแค่ RECEIVED
                if (newStatus != Parcels.Status.RECEIVED) {
                    throw new IllegalArgumentException(
                            "Invalid status transition: PENDING can only change to RECEIVED");
                }
                break;

            case RECEIVED:
                // จาก RECEIVED -> อนุญาตแค่ PICKED_UP
                if (newStatus != Parcels.Status.PICKED_UP) {
                    throw new IllegalArgumentException(
                            "Invalid status transition: RECEIVED can only change to PICKED_UP");
                }
                break;

            case PICKED_UP:
                // จาก PICKED_UP -> ห้ามเปลี่ยนเป็นอย่างอื่นแล้ว
                throw new IllegalArgumentException(
                        "Invalid status transition: cannot change status after PICKED_UP");

            default:
                throw new IllegalArgumentException(
                        "Unknown status: " + oldStatus);
        }
    }
    // 🟥 ADMIN ใช้บังคับเปลี่ยน status (ไม่เช็คกฎ transition)
    public ParcelDetailDto forceUpdateParcelStatus(Integer parcelId, ForceUpdateParcelStatusDto req) {
        Parcels p = parcelsRepository.findById(parcelId)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        Parcels.Status oldStatus = p.getStatus();
        Parcels.Status newStatus = req.getStatus();

        if (newStatus == null) {
            throw new IllegalArgumentException("New status must not be null");
        }

        // ดึงข้อมูล admin ปัจจุบันจาก SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminIdentifier = auth != null ? auth.getName() : "UNKNOWN_ADMIN";

        // log ก่อนเปลี่ยน
        log.info(
                "ADMIN {} forces parcel {} status from {} to {}. Note: {}",
                adminIdentifier,
                parcelId,
                oldStatus,
                newStatus,
                req.getNote()
        );

        // 🔥 ไม่ใช้ validateStatusTransition() ที่เข้มสำหรับ staff
        if (oldStatus != newStatus) {
            p.setStatus(newStatus);

            // ถ้าในระบบอยากให้ pickedUpAt ตรงกับสถานะปัจจุบันด้วย
            if (newStatus == Parcels.Status.PICKED_UP && p.getPickedUpAt() == null) {
                p.setPickedUpAt(java.time.LocalDateTime.now());
            }

            // ถ้า admin เปลี่ยนจาก PICKED_UP -> สถานะอื่น
            // จะ "เก็บ" pickedUpAt เดิมไว้เป็นหลักฐาน
            // หรือถ้าอยากล้างเวลา ก็เขียนแบบนี้แทน:
            // if (newStatus != Parcels.Status.PICKED_UP) {
            //     p.setPickedUpAt(null);
            // }
        }

        Parcels updated = parcelsRepository.save(p); // @PreUpdate จะเซ็ต updatedAt ให้อัตโนมัติ

        // map เป็น ParcelDetailDto (reuse logic เดิม)
        Integer companyId = null;
        String companyName = null;
        if (updated.getCompany() != null) {
            companyId = updated.getCompany().getCompanyId();
            companyName = updated.getCompany().getCompanyName();
        }

        Integer residentId = null;
        String residentName = null;
        String roomNumber = null;
        String email = null;
        if (updated.getUser() != null) {
            residentId = updated.getUser().getUserId();
            String firstName = updated.getUser().getFirstName();
            String lastName = updated.getUser().getLastName();
            residentName =
                    (firstName != null ? firstName : "") +
                            (lastName != null ? " " + lastName : "");
            roomNumber = updated.getUser().getRoomNumber();
            email = updated.getUser().getEmail();
        }

        return new ParcelDetailDto(
                updated.getParcelId(),
                updated.getTrackingNumber(),
                updated.getRecipientName(),
                updated.getStatus(),
                updated.getParcelType(),
                updated.getSenderName(),
                updated.getImageUrl(),
                updated.getReceivedAt(),
                updated.getPickedUpAt(),
                updated.getUpdatedAt(),
                companyId,
                companyName,
                residentId,
                residentName,
                roomNumber,
                email
        );
    }
}