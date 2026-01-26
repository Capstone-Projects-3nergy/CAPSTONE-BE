package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.ParcelListItemDto;
import com.nw2.parcel.Dtos.TrashListItemDto;
import com.nw2.parcel.services.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://cp25nw2.sit.kmutt.ac.th",
        "https://cp25nw2.sit.kmutt.ac.th"
})
public class TrashController {

    private final TrashService trashService;

    // ดูของที่อยู่ใน trash
    @GetMapping
    public List<TrashListItemDto> getTrashParcels() {
        return trashService.getTrashParcels();
    }

    // restore
    @PutMapping("/{parcelId}/restore")
    public ResponseEntity<Void> restoreParcel(@PathVariable Integer parcelId) {
        trashService.restoreParcel(parcelId);
        return ResponseEntity.ok().build();
    }

    // delete permanently
    @DeleteMapping("/{parcelId}")
    public ResponseEntity<Map<String, String>> deleteParcelPermanently(@PathVariable Integer parcelId) {
        trashService.deletePermanently(parcelId);
        return ResponseEntity.ok(
                Map.of("message", "Parcel permanently deleted")
        );
    }
}
