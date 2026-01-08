package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.ParcelListItemDto;
import com.nw2.parcel.services.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<ParcelListItemDto> getTrashParcels() {
        return trashService.getTrashParcels();
    }

    // restore
    @PostMapping("/{parcelId}/restore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void restoreParcel(@PathVariable Integer parcelId) {
        trashService.restoreParcel(parcelId);
    }
}
