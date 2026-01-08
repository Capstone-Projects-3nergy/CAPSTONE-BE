package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.ParcelListItemDto;
import com.nw2.parcel.Dtos.TrashListItemDto;
import com.nw2.parcel.entity.Parcels;
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

//    public List<ParcelListItemDto> getTrashParcels() {
//        return parcelsRepository.findAllByIsDeletedTrueOrderByDeletedAtDesc()
//                .stream()
//                .map(p -> new ParcelListItemDto(
//                        p.getParcelId(),
//                        p.getTrackingNumber(),
//                        p.getRecipientName(),
//                        null,
//                        null,
//                        p.getStatus(),
//                        null,
//                        p.getDeletedAt()
//                ))
//                .toList();
//    }
public List<TrashListItemDto> getTrashParcels() {

    return trashRepository.findAll()
            .stream()
            .map(t -> {
                var p = t.getParcel();

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
                        t.getDeletedBy().getFirstName() +
                                " " +
                                t.getDeletedBy().getLastName();

                return new TrashListItemDto(
                        p.getParcelId(),
                        p.getTrackingNumber(),
                        ownerName,
                        roomNumber,
                        contactEmail,
                        t.getDeletedAt(),
                        deletedByName
                );
            })
            .toList();
}

    @Transactional
    public void restoreParcel(Integer parcelId) {

        Parcels parcel = parcelsRepository
                .findByParcelIdAndIsDeletedTrue(parcelId)
                .orElseThrow(() ->
                        new IllegalStateException("Parcel not found in trash")
                );

        if (parcel.getStatus() == Parcels.Status.PICKED_UP) {
            throw new IllegalStateException("Cannot restore a picked-up parcel");
        }

        parcel.setIsDeleted(false);
        parcel.setDeletedAt(null);

        parcelsRepository.save(parcel);
        trashRepository.deleteByParcelParcelId(parcelId);

        log.info("Parcel {} restored from trash", parcelId);
    }

    @Transactional
    public void deletePermanently(Integer parcelId) {

        // ตรวจว่ามีอยู่ใน trash จริง
        trashRepository.findByParcelParcelId(parcelId)
                .orElseThrow(() ->
                        new IllegalStateException("Parcel not found in trash")
                );

        // ลบ trash ก่อน (เพราะมี FK)
        trashRepository.deleteByParcelParcelId(parcelId);

        // ลบ parcel จริง
        parcelsRepository.deleteById(parcelId);

        log.info("Parcel {} permanently deleted", parcelId);
    }

}
