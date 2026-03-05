package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.ParcelListItemDto;
import com.nw2.parcel.Dtos.TrashListItemDto;
import com.nw2.parcel.Dtos.TrashResidentDto;
import com.nw2.parcel.services.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2"
})
@RestController
@RequestMapping("/api/trash")
@RequiredArgsConstructor
public class TrashController {

    private final TrashService trashService;

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

    @GetMapping("/residents")
    public List<TrashResidentDto> getTrashResidents() {
        return trashService.getTrashResidents();
    }

    @PutMapping("/residents/{residentId}/restore")
    public ResponseEntity<Void> restoreResident(@PathVariable Integer residentId) {
        trashService.restoreResident(residentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/residents/{residentId}")
    public ResponseEntity<Map<String, String>> deleteResidentPermanently(
            @PathVariable Integer residentId
    ) {
        trashService.deleteResidentPermanently(residentId);
        return ResponseEntity.ok(
                Map.of("message", "Resident permanently deleted")
        );
    }

}
