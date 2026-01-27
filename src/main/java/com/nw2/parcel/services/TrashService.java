package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.TrashListItemDto;
import com.nw2.parcel.Dtos.TrashResidentDto;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Trash;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.ParcelsRepository;
import com.nw2.parcel.repositories.TrashRepository;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashService {

    private final ParcelsRepository parcelsRepository;
    private final TrashRepository trashRepository;
    private final UsersRepository usersRepository;

    private static final Logger log = LoggerFactory.getLogger(TrashService.class);

    // GET TRASH
    @Transactional(readOnly = true)
    public List<TrashListItemDto> getTrashParcels() {

        return trashRepository
                .findAllByTargetTypeOrderByDeletedAtDesc(Trash.TargetType.PARCEL)
                .stream()
                .map(t -> {

                    Parcels p = parcelsRepository.findById(t.getTargetId())
                            .orElseThrow();

                    String ownerName;
                    String roomNumber = null;
                    String contactEmail = null;

                    if (p.getUser() != null) {
                        ownerName =
                                (p.getUser().getFirstName() != null ? p.getUser().getFirstName() : "") +
                                        (p.getUser().getLastName() != null ? " " + p.getUser().getLastName() : "");
                        roomNumber = p.getUser().getRoomNumber();
                        contactEmail = p.getUser().getEmail();
                    } else {
                        ownerName = p.getRecipientName();
                    }

                    String deletedByName =
                            t.getDeletedBy().getFirstName() + " " +
                                    t.getDeletedBy().getLastName();

                    return new TrashListItemDto(
                            p.getParcelId(),
                            p.getTrackingNumber(),
                            ownerName,
                            roomNumber,
                            contactEmail,
                            p.getStatus(),
                            t.getDeletedAt(),
                            deletedByName
                    );
                })
                .toList();
    }

    // RESTORE
    @Transactional
    public void restoreParcel(Integer parcelId) {

        Trash trash = trashRepository
                .findByTargetTypeAndTargetId(Trash.TargetType.PARCEL, parcelId)
                .orElseThrow(() -> new IllegalStateException("Parcel not found in trash"));

        Parcels parcel = parcelsRepository.findById(parcelId)
                .orElseThrow();

        parcel.setIsDeleted(false);
        parcel.setDeletedAt(null);

        parcelsRepository.save(parcel);
        trashRepository.delete(trash);

        log.info("Parcel {} restored from trash", parcelId);
    }


    // DELETE PERMANENTLY
    @Transactional
    public void deletePermanently(Integer parcelId) {

        Trash trash = trashRepository
                .findByTargetTypeAndTargetId(Trash.TargetType.PARCEL, parcelId)
                .orElseThrow(() -> new IllegalStateException("Parcel not found in trash"));

        parcelsRepository.deleteById(parcelId);
        trashRepository.delete(trash);

        log.info("Parcel {} permanently deleted", parcelId);
    }

    @Transactional(readOnly = true)
    public List<TrashResidentDto> getTrashResidents() {

        return trashRepository
                .findAllByTargetTypeOrderByDeletedAtDesc(Trash.TargetType.USER)
                .stream()
                .map(trash -> {

                    Users u = usersRepository.findById(trash.getTargetId())
                            .orElseThrow(() ->
                                    new IllegalArgumentException("User not found in trash")
                            );

                    String deletedByName =
                            trash.getDeletedBy().getFirstName() + " " +
                                    trash.getDeletedBy().getLastName();

                    return new TrashResidentDto(
                            u.getUserId(),
                            u.getEmail(),
                            u.getFirstName(),
                            u.getLastName(),
                            u.getPhoneNumber(),
                            u.getLineId(),
                            u.getRoomNumber(),
                            null, // position
                            u.getProfileImageUrl(),
                            u.getRole().name(),
                            u.getStatus().name(),
                            trash.getDeletedAt(),
                            deletedByName
                    );
                })
                .toList();
    }

    @Transactional
    public void restoreResident(Integer residentId) {

        Trash trash = trashRepository
                .findByTargetTypeAndTargetId(Trash.TargetType.USER, residentId)
                .orElseThrow(() -> new IllegalStateException("Resident not found in trash"));

        Users resident = usersRepository.findById(residentId)
                .orElseThrow();

        resident.setStatus(Users.Status.ACTIVE);
        resident.setDeletedAt(null);

        usersRepository.save(resident);
        trashRepository.delete(trash);

        log.info("Resident {} restored from trash", residentId);
    }

    @Transactional
    public void deleteResidentPermanently(Integer residentId) {

        Trash trash = trashRepository
                .findByTargetTypeAndTargetId(Trash.TargetType.USER, residentId)
                .orElseThrow(() -> new IllegalStateException("Resident not found in trash"));

        usersRepository.deleteById(residentId);
        trashRepository.delete(trash);

        log.info("Resident {} permanently deleted", residentId);
    }

}
