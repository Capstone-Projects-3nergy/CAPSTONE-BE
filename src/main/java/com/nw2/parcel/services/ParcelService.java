package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.*;
import com.nw2.parcel.entity.*;
import com.nw2.parcel.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import static com.nw2.parcel.services.ParcelSpecs.*;
import java.time.LocalDateTime;

@Service
public class ParcelService {

    private final ParcelsRepository parcelsRepository;
    private final UsersRepository usersRepository;
    private final CompaniesRepository companiesRepository;
    private final NotificationRepository notificationRepository;

    public ParcelService(ParcelsRepository parcelsRepository,
                         UsersRepository usersRepository,
                         CompaniesRepository companiesRepository,
                         NotificationRepository notificationRepository) {
        this.parcelsRepository = parcelsRepository;
        this.usersRepository = usersRepository;
        this.companiesRepository = companiesRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Parcels addParcel(AddParcelRequest req, Users staffUser) {
        if (staffUser.getRole() != Users.Role.STAFF) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only STAFF can add parcels");
        }

        // ตรวจสอบ Resident
        Users resident = usersRepository.findById(req.residentUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Resident not found"));

        // ตรวจสอบหรือสร้างบริษัทขนส่ง
        Company company = null;
        if (req.companyId() != null) {
            company = companiesRepository.findById(req.companyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));
        } else if (req.companyName() != null && !req.companyName().isBlank()) {
            company = companiesRepository.findByCompanyNameIgnoreCase(req.companyName())
                    .orElseGet(() -> {
                        Company newCompany = new Company();
                        newCompany.setCompanyName(req.companyName().trim());
                        return companiesRepository.save(newCompany);
                    });
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company is required");
        }

        // บันทึก Parcel
        Parcels parcel = new Parcels();
        parcel.setTrackingNumber(req.trackingNumber());
        parcel.setRecipientName(req.recipientName());
        parcel.setCompany(company);
        parcel.setUser(resident);
        parcel.setParcelType(req.parcelType());
        parcel.setSenderName(req.senderName());
        parcel.setImageUrl(req.imageUrl());
        parcel.setStatus(Parcels.Status.PENDING);
        parcel.setReceivedAt(LocalDateTime.now());

        Parcels saved = parcelsRepository.saveAndFlush(parcel);

        // สร้าง Notification (แต่ยังไม่ส่งจริง)
        Notification noti = new Notification();
        noti.setNotiTitle("📦 พัสดุใหม่ถึงหอพักของคุณ");
        noti.setNotiMessage("พัสดุหมายเลขติดตาม: " + saved.getTrackingNumber());
        noti.setStatus(Notification.Status.PENDING);
        noti.setNotificationType(Notification.Type.LINE);
        noti.setParcel(saved);
        noti.setUser(resident);
        notificationRepository.save(noti);

        return saved;
    }
    public PageResponse<ParcelListItemDto> listParcels(
            String q, String status, LocalDateTime dayStart, LocalDateTime dayEnd,
            int page, int size, Sort sort) {

        Specification<Parcels> spec = Specification.where(keyword(q)).and(status(status));
        if (dayStart != null && dayEnd != null) {
            spec = spec.and((root, cq, cb) -> cb.between(root.get("receivedAt"), dayStart, dayEnd));
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Parcels> pg = parcelsRepository.findAll(spec, pageable);

        var content = pg.map(p -> new ParcelListItemDto(
                p.getParcelId(),
                p.getTrackingNumber(),
                p.getCompany() != null ? p.getCompany().getCompanyName() : null,
                p.getRecipientName(),
                p.getUser() != null ? p.getUser().getRoomNumber() : null,
                p.getUser() != null ? p.getUser().getPhoneNumber() : null,
                p.getStatus().name(),
                p.getReceivedAt()
        )).toList();

        return new PageResponse<>(content, page, size, pg.getTotalElements(), pg.getTotalPages());
    }

    public ParcelDetailDto getParcelDetail(Integer parcelId) {
        Parcels p = parcelsRepository.findById(parcelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parcel not found"));

        Users u = p.getUser();
        Company c = p.getCompany();

        return new ParcelDetailDto(
                p.getParcelId(),
                p.getTrackingNumber(),
                p.getStatus(),
                p.getParcelType(),
                p.getSenderName(),
                p.getImageUrl(),
                p.getReceivedAt(),
                p.getPickedUpAt(),
                c != null ? c.getCompanyId() : null,
                c != null ? c.getCompanyName() : null,
                u != null ? u.getUserId() : null,
                u != null ? (u.getFirstName() + " " + u.getLastName()) : null,
                u != null ? u.getPhoneNumber() : null,
                u != null ? u.getRoomNumber() : null,
                (u != null && u.getDorm() != null) ? u.getDorm().getDormName() : null
        );
    }

    @Transactional
    public Parcels updateParcel(EditParcelRequest req) {
        Parcels p = parcelsRepository.findById(req.parcelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parcel not found"));

        // ❌ ห้ามแก้ tracking number — ไม่แตะ p.setTrackingNumber()

        // สถานะ / เวลา
        if (req.status() != null) {
            p.setStatus(req.status());
            if (req.status() == Parcels.Status.PICKED_UP) {
                p.setPickedUpAt(req.pickedUpAt() != null ? req.pickedUpAt() : LocalDateTime.now());
            } else if (req.status() == Parcels.Status.RECEIVED || req.status() == Parcels.Status.PENDING) {
                p.setPickedUpAt(null);
            }
        }

        if (req.parcelType() != null) p.setParcelType(req.parcelType());
        if (req.senderName() != null) p.setSenderName(req.senderName());
        if (req.imageUrl() != null) p.setImageUrl(req.imageUrl());

        // บริษัท: เลือกจาก id หรือสร้างจากชื่อใหม่
        if (req.companyId() != null) {
            Company c = companiesRepository.findById(req.companyId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Company not found"));
            p.setCompany(c);
        } else if (req.companyName() != null && !req.companyName().isBlank()) {
            Company c = companiesRepository.findByCompanyNameIgnoreCase(req.companyName())
                    .orElseGet(() -> {
                        Company newC = new Company();
                        newC.setCompanyName(req.companyName().trim());
                        return companiesRepository.save(newC);
                    });
            p.setCompany(c);
        }

        p.setUpdatedAt(LocalDateTime.now());
        return parcelsRepository.save(p);
    }
}