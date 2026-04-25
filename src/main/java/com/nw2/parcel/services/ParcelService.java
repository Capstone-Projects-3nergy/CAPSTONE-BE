package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.*;
import com.nw2.parcel.entity.*;
import com.nw2.parcel.exception.ConflictException;
import com.nw2.parcel.exception.ParcelNotFoundException;
import com.nw2.parcel.exception.ResourceNotFoundException;
import com.nw2.parcel.exception.UnauthorizedException;
import com.nw2.parcel.repositories.ParcelsRepository;
import com.nw2.parcel.repositories.CompanyRepository;
import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.repositories.TrashRepository;
import com.nw2.parcel.repositories.ParcelVerificationRepository;
import com.nw2.parcel.repositories.NotificationService;
import com.nw2.parcel.entity.ParcelStatusLog;
import com.nw2.parcel.repositories.ParcelStatusLogRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParcelService {

    private static final Logger log = LoggerFactory.getLogger(ParcelService.class);
    private static final long OVERDUE_THRESHOLD_DAYS = 1;

    private final ParcelsRepository parcelsRepository;
    private final CompanyRepository companyRepository;
    private final UsersRepository usersRepository;
    private final TrashRepository trashRepository;
    private final ParcelVerificationRepository verificationRepository;
    private final NotificationService notificationService;
    private final ParcelStatusLogRepository statusLogRepository; // ✅ เพิ่มใหม่

    // ─── Overdue Helpers ────────────────────────────────────────────────────────

    private boolean isOverdue(Parcels p) {
        return (p.getStatus() == Parcels.Status.WAITING || p.getStatus() == Parcels.Status.OVERDUE)
                && p.getReceivedAt() != null
                && p.getReceivedAt().plusDays(OVERDUE_THRESHOLD_DAYS).isBefore(LocalDateTime.now());
    }

    private long calcOverdueDays(Parcels p) {
        if (!isOverdue(p)) return 0L;
        return ChronoUnit.DAYS.between(
                p.getReceivedAt().plusDays(OVERDUE_THRESHOLD_DAYS),
                LocalDateTime.now()
        );
    }

    // ─── Status Log Helper ───────────────────────────────────────────────────────

    /**
     * บันทึก log ทุกครั้งที่ status เปลี่ยน
     * changedBy = null หมายถึง system/scheduler เป็นคนเปลี่ยน
     */
    private void logStatusChange(Parcels parcel,
                                  Parcels.Status oldStatus,
                                  Parcels.Status newStatus,
                                  Users changedBy,
                                  String note) {
        if (oldStatus == newStatus) return; // ไม่มีการเปลี่ยนจริง ไม่ต้อง log

        ParcelStatusLog entry = new ParcelStatusLog();
        entry.setParcel(parcel);
        entry.setOldStatus(oldStatus);   // null = สร้างใหม่ครั้งแรก
        entry.setNewStatus(newStatus);
        entry.setChangedBy(changedBy);   // null = system
        entry.setNote(note);
        entry.setChangedAt(LocalDateTime.now());
        statusLogRepository.save(entry);
    }

    /**
     * Helper: ดึง Users entity ของ user ที่ล็อกอินอยู่ (ไม่ throw ถ้าไม่เจอ → คืน null)
     * ใช้สำหรับบันทึก changedBy ใน log
     */
    private Users getCurrentUserEntityOrNull() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) return null;
            String firebaseUid = auth.getName();
            return usersRepository.findByFirebaseUid(firebaseUid).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Create ─────────────────────────────────────────────────────────────────

    public Parcels createParcel(CreateParcelDto req) {

        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found"));

        Users resident = usersRepository
                .findByUserIdAndRole(req.getUserId(), Users.Role.RESIDENT)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

        Parcels parcel = new Parcels();
        parcel.setTrackingNumber(req.getTrackingNumber());
        parcel.setRecipientName(req.getRecipientName());
        parcel.setParcelType(req.getParcelType());
        parcel.setSenderName(req.getSenderName());
        parcel.setStatus(Parcels.Status.WAITING);
        parcel.setCompany(company);
        parcel.setUser(resident);

        Users matchedResident = autoAssignResidentIfMatched(parcel);

        Parcels savedParcel = parcelsRepository.save(parcel);

        // ✅ Log: สร้างพัสดุใหม่ (oldStatus = null)
        Users creator = getCurrentUserEntityOrNull();
        logStatusChange(savedParcel, null, savedParcel.getStatus(), creator, "Parcel created by staff");

        if (matchedResident != null) {
            notificationService.notifyParcelMultiChannel(savedParcel, matchedResident);
        }

        return savedParcel;
    }

    // ─── Staff: List & Detail ────────────────────────────────────────────────────

    public List<ParcelListItemDto> getAllParcelsForStaff() {
        List<Parcels> parcels = parcelsRepository.findAllByIsDeletedFalseOrderByReceivedAtDesc();

        return parcels.stream()
                .map(p -> {
                    String ownerName;
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
                            p.getReceivedAt(),
                            p.getUpdatedAt(),
                            isOverdue(p),
                            calcOverdueDays(p)
                    );
                })
                .collect(Collectors.toList());
    }

    public ParcelDetailDto getParcelDetail(Integer parcelId) {
        Parcels p = parcelsRepository.findByParcelIdAndIsDeletedFalse(parcelId)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));
        return toDetailDto(p);
    }

    // ─── Staff: Update ───────────────────────────────────────────────────────────

    public ParcelDetailDto updateParcelForStaff(Integer parcelId, UpdateParcelDto req) {
        Parcels p = parcelsRepository.findByParcelIdAndIsDeletedFalse(parcelId)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        Parcels.Status oldStatus = p.getStatus(); // ✅ เก็บ status เดิมก่อนแก้ไข
        boolean assignedNewResident = false;

        if (req.getTrackingNumber() != null) p.setTrackingNumber(req.getTrackingNumber());
        if (req.getRecipientName() != null) p.setRecipientName(req.getRecipientName());
        if (req.getParcelType() != null) p.setParcelType(req.getParcelType());
        if (req.getSenderName() != null) p.setSenderName(req.getSenderName());
        if (req.getImageUrl() != null) p.setImageUrl(req.getImageUrl());

        if (req.getCompanyId() != null
                && (p.getCompany() == null || !req.getCompanyId().equals(p.getCompany().getCompanyId()))) {
            Company company = companyRepository.findById(req.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + req.getCompanyId()));
            p.setCompany(company);
        }

        if (req.getUserId() != null) {
            if (p.getUser() == null || !p.getUser().getUserId().equals(req.getUserId())) {
                Users resident = usersRepository
                        .findByUserIdAndRole(req.getUserId(), Users.Role.RESIDENT)
                        .orElseThrow(() -> new IllegalArgumentException("Resident not found with id: " + req.getUserId()));
                p.setUser(resident);
                assignedNewResident = true;
            }
        }

        Parcels.Status newStatus = req.getStatus();

        if (assignedNewResident && p.getStatus() == Parcels.Status.WAITING_FOR_STAFF) {
            newStatus = Parcels.Status.WAITING;
        }

        if (newStatus != null) {
            p.setStatus(newStatus);
            if (newStatus == Parcels.Status.PICKED_UP) {
                if (p.getPickedUpAt() == null) p.setPickedUpAt(LocalDateTime.now());
            } else {
                p.setPickedUpAt(null);
            }
        }

        Parcels updated = parcelsRepository.save(p);

        // ✅ Log: บันทึกการเปลี่ยน status (ถ้าเปลี่ยนจริง)
        Parcels.Status finalStatus = updated.getStatus();
        if (oldStatus != finalStatus) {
            Users staff = getCurrentUserEntityOrNull();
            logStatusChange(updated, oldStatus, finalStatus, staff, "Updated by staff");
        }

        return toDetailDto(updated);
    }

    // ─── Admin: Force Update Status ──────────────────────────────────────────────

    public ParcelDetailDto forceUpdateParcelStatus(Integer parcelId, ForceUpdateParcelStatusDto req) {
        Parcels p = parcelsRepository.findByParcelIdAndIsDeletedFalse(parcelId)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        Parcels.Status oldStatus = p.getStatus(); // ✅ เก็บ status เดิม
        Parcels.Status newStatus = req.getStatus();

        if (newStatus == null) {
            throw new IllegalArgumentException("New status must not be null");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminIdentifier = auth != null ? auth.getName() : "UNKNOWN_ADMIN";

        log.info(
                "ADMIN {} forces parcel {} status from {} to {}. Note: {}",
                adminIdentifier, parcelId, oldStatus, newStatus, req.getNote()
        );

        p.setStatus(newStatus);

        if (newStatus == Parcels.Status.PICKED_UP) {
            if (p.getPickedUpAt() == null) p.setPickedUpAt(LocalDateTime.now());
        } else {
            p.setPickedUpAt(null);
        }

        Parcels updated = parcelsRepository.save(p);

        // ✅ Log: Admin force update พร้อม note
        Users admin = getCurrentUserEntityOrNull();
        logStatusChange(updated, oldStatus, newStatus, admin,
                req.getNote() != null ? "[ADMIN FORCE] " + req.getNote() : "[ADMIN FORCE]");

        return toDetailDto(updated);
    }

    // ─── Resident ────────────────────────────────────────────────────────────────

    private Users getCurrentResident() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new UnauthorizedException("No authenticated user");
        }

        org.springframework.security.core.userdetails.User principal =
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();

        String firebaseUid = principal.getUsername();

        Users u = usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new UnauthorizedException("User not found in system"));

        if (u.getRole() != Users.Role.RESIDENT) {
            throw new IllegalArgumentException("Current user is not a RESIDENT");
        }

        return u;
    }

    public List<ParcelListItemDto> getParcelsForCurrentResident() {
        Users currentResident = getCurrentResident();

        List<Parcels> parcels = parcelsRepository
                .findMatchedParcelsByUserId(currentResident.getUserId());

        return parcels.stream()
                .map(p -> {
                    String ownerName;
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
                            p.getReceivedAt(),
                            p.getUpdatedAt(),
                            isOverdue(p),
                            calcOverdueDays(p)
                    );
                })
                .toList();
    }

    public ParcelDetailDto getParcelDetailForResident(Integer parcelId) {
        Users currentResident = getCurrentResident();

        Parcels p = parcelsRepository
                .findByParcelIdAndUserUserIdAndIsDeletedFalse(parcelId, currentResident.getUserId())
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        return toDetailDto(p);
    }

    // Resident confirm pickup — รองรับ OVERDUE ด้วย
    public ParcelDetailDto confirmParcelReceivedByResident(Integer parcelId) {
        Users currentResident = getCurrentResident();

        Parcels p = parcelsRepository
                .findByParcelIdAndUserUserIdAndIsDeletedFalse(parcelId, currentResident.getUserId())
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        // รองรับทั้ง WAITING และ OVERDUE
        if (p.getStatus() != Parcels.Status.WAITING && p.getStatus() != Parcels.Status.OVERDUE) {
            throw new ConflictException(
                    "Parcel cannot be confirmed in current status: " + p.getStatus()
            );
        }

        Parcels.Status oldStatus = p.getStatus(); // ✅ เก็บ status เดิม

        p.setStatus(Parcels.Status.PICKED_UP);
        if (p.getPickedUpAt() == null) p.setPickedUpAt(LocalDateTime.now());

        Parcels updated = parcelsRepository.save(p);

        // ✅ Log: Resident กด confirm รับพัสดุ
        logStatusChange(updated, oldStatus, Parcels.Status.PICKED_UP, currentResident, "Confirmed by resident");

        return toDetailDto(updated);
    }

    // ─── Misc ────────────────────────────────────────────────────────────────────

    public List<String> getParcelTypes() {
        return Arrays.stream(Parcels.Parceltype.values())
                .map(Enum::name)
                .toList();
    }

    public Parcels createParcelFromPublicForm(SenderCreateParcelDto req) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + req.getCompanyId()));

        Parcels parcel = new Parcels();
        parcel.setTrackingNumber(req.getTrackingNumber());
        parcel.setRecipientName(req.getRecipientName());
        parcel.setParcelType(req.getParcelType());
        parcel.setSenderName(req.getSenderName());
        parcel.setCompany(company);
        parcel.setStatus(Parcels.Status.WAITING_FOR_STAFF);
        parcel.setUser(null);

        Users matchedResident = autoAssignResidentIfMatched(parcel);

        Parcels saved = parcelsRepository.save(parcel);

        // ✅ Log: สร้างจาก public form (changedBy = null เพราะยังไม่รู้ว่าใคร)
        logStatusChange(saved, null, saved.getStatus(), null, "Created via public sender form");

        if (matchedResident != null) {
            notificationService.notifyParcelMultiChannel(saved, matchedResident);
        }

        return saved;
    }

    public void moveParcelToTrash(Integer parcelId) {
        Parcels parcel = parcelsRepository
                .findByParcelIdAndIsDeletedFalse(parcelId)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        if (parcel.getStatus() == Parcels.Status.PICKED_UP) {
            throw new ConflictException("Cannot delete a picked-up parcel");
        }

        parcel.setIsDeleted(true);
        parcel.setDeletedAt(LocalDateTime.now());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String firebaseUid = auth.getName();

        Users staff = usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        Trash trash = new Trash();
        trash.setTargetType(Trash.TargetType.PARCEL);
        trash.setTargetId(parcelId);
        trash.setDeletedAt(LocalDateTime.now());
        trash.setDeletedBy(staff);

        parcelsRepository.save(parcel);
        trashRepository.save(trash);

        // ✅ Log: บันทึกว่าถูกย้ายไป trash (ไม่ใช่ status change แต่ note ไว้)
        logStatusChange(parcel, parcel.getStatus(), parcel.getStatus(), staff, "Moved to trash");
    }

    public List<ParcelListItemDto> getTrashParcels() {
        return parcelsRepository.findAllByIsDeletedTrueOrderByDeletedAtDesc()
                .stream()
                .map(p -> new ParcelListItemDto(
                        p.getParcelId(),
                        p.getTrackingNumber(),
                        p.getRecipientName(),
                        null,
                        null,
                        p.getStatus(),
                        p.getReceivedAt(),
                        p.getDeletedAt(),
                        false,
                        0L
                ))
                .toList();
    }

    // ─── Overdue ─────────────────────────────────────────────────────────────────

    /**
     * ดึง parcel ที่ overdue จริงๆ (query ทั้ง WAITING และ OVERDUE ที่เกิน threshold)
     */
    public List<Parcels> getOverdueParcels() {
        List<Parcels> parcels = parcelsRepository.findByStatusInAndIsDeletedFalse(
                List.of(Parcels.Status.WAITING, Parcels.Status.OVERDUE)
        );
        LocalDateTime now = LocalDateTime.now();
        return parcels.stream()
                .filter(p -> p.getReceivedAt().plusDays(OVERDUE_THRESHOLD_DAYS).isBefore(now))
                .toList();
    }

    /**
     * เรียกจาก Scheduler เมื่อ mark parcel เป็น OVERDUE
     * แยก method ออกมาเพื่อให้ Scheduler ใช้แล้วได้ log ด้วย
     */
    public void markParcelAsOverdue(Parcels parcel) {
        Parcels.Status oldStatus = parcel.getStatus();
        if (oldStatus == Parcels.Status.OVERDUE) return; // mark ซ้ำ ไม่ต้องทำ

        parcel.setStatus(Parcels.Status.OVERDUE);
        Parcels saved = parcelsRepository.save(parcel);

        // ✅ Log: system/scheduler เป็นคนเปลี่ยน (changedBy = null)
        logStatusChange(saved, oldStatus, Parcels.Status.OVERDUE, null, "Auto-marked overdue by scheduler");
    }

    // ─── Parcel Status History (สำหรับ Detail Page / Dashboard) ─────────────────

    /**
     * ดึง status history ของพัสดุชิ้นหนึ่ง เรียงตามเวลา (เก่า → ใหม่)
     */
    public List<ParcelStatusLog> getParcelStatusHistory(Integer parcelId) {
        // ตรวจสอบว่า parcel มีอยู่จริงก่อน
        parcelsRepository.findByParcelIdAndIsDeletedFalse(parcelId)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));
        return statusLogRepository.findByParcelParcelIdOrderByChangedAtAsc(parcelId);
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────────

    private Users autoAssignResidentIfMatched(Parcels parcel) {
        String normalizedTracking = parcel.getTrackingNumber().trim().toUpperCase();
        parcel.setTrackingNumber(normalizedTracking);

        return verificationRepository
                .findByTrackingNumberIgnoreCaseAndVerifiedFalse(normalizedTracking)
                .map(pv -> {
                    Users resident = pv.getResident();
                    parcel.setUser(resident);
                    pv.setVerified(true);
                    verificationRepository.save(pv);
                    return resident;
                })
                .orElse(null);
    }

    /**
     * Helper แปลง Parcels entity → ParcelDetailDto (ลด code ซ้ำ)
     */
    private ParcelDetailDto toDetailDto(Parcels p) {
        Integer companyId = null;
        String companyName = null;
        if (p.getCompany() != null) {
            companyId = p.getCompany().getCompanyId();
            companyName = p.getCompany().getCompanyName();
        }

        Integer residentId = null;
        String residentName = null;
        String roomNumber = null;
        String email = null;
        if (p.getUser() != null) {
            residentId = p.getUser().getUserId();
            residentName = (p.getUser().getFirstName() != null ? p.getUser().getFirstName() : "")
                    + (p.getUser().getLastName() != null ? " " + p.getUser().getLastName() : "");
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
                email,
                isOverdue(p),
                calcOverdueDays(p)
        );
    }
}
