package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.TrashListItemDto;
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

    @Transactional
    public void deleteResident(Integer residentId, Users staff) {

        Users resident = usersRepository
                .findByUserIdAndRole(residentId, Users.Role.RESIDENT)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));

        // ป้องกันลบซ้ำ
        if (resident.getStatus() == Users.Status.DELETED) {
            throw new IllegalStateException("Resident already deleted");
        }

        resident.setStatus(Users.Status.DELETED);
        resident.setDeletedAt(LocalDateTime.now());

        Trash trash = new Trash();
        trash.setTargetType(Trash.TargetType.USER);
        trash.setTargetId(residentId);
        trash.setDeletedAt(LocalDateTime.now());
        trash.setDeletedBy(staff);

        usersRepository.save(resident);
        trashRepository.save(trash);
    }
}
