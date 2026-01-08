package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.ParcelListItemDto;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.repositories.ParcelsRepository;
import com.nw2.parcel.repositories.TrashRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashService {

    private final ParcelsRepository parcelsRepository;
    private final TrashRepository trashRepository;
    private static final Logger log = LoggerFactory.getLogger(TrashService.class);

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

}
