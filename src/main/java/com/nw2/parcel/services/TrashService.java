package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.TrashListItemDto;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Trash;
import com.nw2.parcel.repositories.ParcelsRepository;
import com.nw2.parcel.repositories.TrashRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashService {

    private final ParcelsRepository parcelsRepository;
    private final TrashRepository trashRepository;

    private static final Logger log = LoggerFactory.getLogger(TrashService.class);

    // GET TRASH
    @Transactional(readOnly = true)
    public List<TrashListItemDto> getTrashParcels() {

        return trashRepository.findAll()
                .stream()
                .map(t -> {
                    Parcels p = t.getParcel();

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

        Trash trash = trashRepository.findByParcelParcelId(parcelId)
                .orElseThrow(() ->
                        new IllegalStateException("Parcel not found in trash")
                );

        Parcels parcel = trash.getParcel();

        /* 1) restore parcel */
        parcel.setIsDeleted(false);
        parcel.setDeletedAt(null);

        /* 2) ตัด relation เพื่อ orphanRemoval */
        parcel.setTrash(null);

        parcelsRepository.save(parcel);
        trashRepository.delete(trash);

        log.info("Parcel {} restored from trash", parcelId);
    }


    // DELETE PERMANENTLY
    @Transactional
    public void deletePermanently(Integer parcelId) {

        Trash trash = trashRepository.findByParcelParcelId(parcelId)
                .orElseThrow(() ->
                        new IllegalStateException("Parcel not found in trash")
                );

        Parcels parcel = trash.getParcel();

        /* 🔥 ตัด relation */
        parcel.setTrash(null);

        trashRepository.delete(trash);
        parcelsRepository.delete(parcel);

        log.info("Parcel {} permanently deleted", parcelId);
    }
}
