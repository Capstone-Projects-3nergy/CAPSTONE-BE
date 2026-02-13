package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.*;
import com.nw2.parcel.entity.Company;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Trash;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.exception.ConflictException;
import com.nw2.parcel.exception.ParcelNotFoundException;
import com.nw2.parcel.exception.ResourceNotFoundException;
import com.nw2.parcel.exception.UnauthorizedException;
import com.nw2.parcel.repositories.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParcelService {

    private static final Logger log = LoggerFactory.getLogger(ParcelService.class);

    private final ParcelsRepository parcelsRepository;
    private final CompanyRepository companyRepository;
    private final UsersRepository usersRepository;
    private final TrashRepository trashRepository;
    private final ParcelVerificationRepository verificationRepository;
    private final NotificationService notificationService;

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
        parcel.setStatus(Parcels.Status.RECEIVED);
        parcel.setCompany(company);
        parcel.setUser(resident);

        Users matchedResident = autoAssignResidentIfMatched(parcel);

        Parcels savedParcel = parcelsRepository.save(parcel); // 🔥 save ก่อน

        if (matchedResident != null) {
            notificationService.notifyParcelMultiChannel(savedParcel, matchedResident);
        }

        return savedParcel;
    }


    // view
    public List<ParcelListItemDto> getAllParcelsForStaff() {
        List<Parcels> parcels = parcelsRepository.findAllByIsDeletedFalseOrderByReceivedAtDesc();
//        List<Parcels> parcels = parcelsRepository.findAllByOrderByReceivedAtDesc();
//        List<Parcels> parcelsDelete = parcelsRepository.findAllByIsDeletedFalseOrderByReceivedAtDesc();

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
                            p.getReceivedAt(),
                            p.getUpdatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    // details
    public ParcelDetailDto getParcelDetail(Integer parcelId) {

        Parcels p = parcelsRepository.findByParcelIdAndIsDeletedFalse(parcelId)
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

    //edit parcel + status สำหรับ STAFF
    public ParcelDetailDto updateParcelForStaff(Integer parcelId, UpdateParcelDto req) {
        Parcels p = parcelsRepository.findByParcelIdAndIsDeletedFalse(parcelId)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        boolean assignedNewResident = false;

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
                            new ResourceNotFoundException("Company not found: " + req.getCompanyId())
                    );
            p.setCompany(company);
        }

        if (req.getUserId() != null) {
            if (p.getUser() == null || !p.getUser().getUserId().equals(req.getUserId())) {
                Users resident = usersRepository
                        .findByUserIdAndRole(req.getUserId(), Users.Role.RESIDENT)
                        .orElseThrow(() ->
                                new IllegalArgumentException("Resident not found with id: " + req.getUserId())
                        );
                p.setUser(resident);
                assignedNewResident = true;
            }
        }

        Parcels.Status newStatus = req.getStatus();

        if (assignedNewResident && p.getStatus() == Parcels.Status.WAITING_FOR_STAFF) {
            newStatus = Parcels.Status.RECEIVED;
        }

        if (newStatus != null) {
            p.setStatus(newStatus);

            if (newStatus == Parcels.Status.PICKED_UP) {
                if (p.getPickedUpAt() == null) {
                    p.setPickedUpAt(LocalDateTime.now());
                }
            } else {
                p.setPickedUpAt(null);
            }
        }

        Parcels updated = parcelsRepository.save(p);

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


    public ParcelDetailDto forceUpdateParcelStatus(Integer parcelId, ForceUpdateParcelStatusDto req) {
        Parcels p = parcelsRepository.findByParcelIdAndIsDeletedFalse(parcelId)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        Parcels.Status oldStatus = p.getStatus();
        Parcels.Status newStatus = req.getStatus();

        if (newStatus == null) {
            throw new IllegalArgumentException("New status must not be null");
        }

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

        p.setStatus(newStatus);

        if (newStatus == Parcels.Status.PICKED_UP) {
            if (p.getPickedUpAt() == null) {
                p.setPickedUpAt(java.time.LocalDateTime.now());
            }
        } else {
            // RECEIVED
            p.setPickedUpAt(null);
        }

        Parcels updated = parcelsRepository.save(p);

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

    // resident list ของตัวเอง
    public List<ParcelListItemDto> getParcelsForCurrentResident() {
        Users currentResident = getCurrentResident();

        List<Parcels> parcels =
                parcelsRepository
                        .findByUserUserIdAndIsDeletedFalseOrderByReceivedAtDesc(
                                currentResident.getUserId()
                        );

        return parcels.stream()
                .map(p -> {
                    String ownerName = null;
                    String roomNumber = null;
                    String contactEmail = null;

                    if (p.getUser() != null) {
                        String firstName = p.getUser().getFirstName();
                        String lastName = p.getUser().getLastName();
                        ownerName =
                                (firstName != null ? firstName : "") +
                                        (lastName != null ? " " + lastName : "");
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
                            p.getUpdatedAt()
                    );
                })
                .toList();
    }

    // VIEW-PARCEL-DETAIL (เฉพาะของตัวเองเท่านั้น)
    public ParcelDetailDto getParcelDetailForResident(Integer parcelId) {
        Users currentResident = getCurrentResident();

        Parcels p = parcelsRepository
                .findByParcelIdAndUserUserIdAndIsDeletedFalse(parcelId, currentResident.getUserId())
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

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

    public ParcelDetailDto confirmParcelReceivedByResident(Integer parcelId) {
        Users currentResident = getCurrentResident();

        Parcels p = parcelsRepository
                .findByParcelIdAndUserUserIdAndIsDeletedFalse(parcelId, currentResident.getUserId())
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));

        if (p.getStatus() != Parcels.Status.RECEIVED) {
            throw new ConflictException(
                    "Parcel cannot be confirmed in current status: " + p.getStatus()
            );
        }

        Parcels.Status oldStatus = p.getStatus();

        p.setStatus(Parcels.Status.PICKED_UP);

        if (p.getPickedUpAt() == null) {
            p.setPickedUpAt(LocalDateTime.now());
        }

        Parcels updated = parcelsRepository.save(p);

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

    public List<String> getParcelTypes() {
        return Arrays.stream(Parcels.Parceltype.values())
                .map(Enum::name)
                .toList();
    }

    public Parcels createParcelFromPublicForm(SenderCreateParcelDto req) {
        Company company = companyRepository.findById(req.getCompanyId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Company not found: " + req.getCompanyId())
                );

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
                        p.getDeletedAt()
                ))
                .toList();
    }

    private Users autoAssignResidentIfMatched(Parcels parcel) {

        String normalizedTracking = parcel.getTrackingNumber()
                .trim()
                .toUpperCase();

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

}