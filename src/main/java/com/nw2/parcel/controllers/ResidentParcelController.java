package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.ParcelDetailDto;
import com.nw2.parcel.Dtos.ParcelListItemDto;
import com.nw2.parcel.services.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://cp25nw2.sit.kmutt.ac.th",
        "https://cp25nw2.sit.kmutt.ac.th",
        "http://cp25nw2.sit.kmutt.ac.th/capstone25/cp25nw2",
        "https://cp25nw2.sit.kmutt.ac.th/capstone25/cp25nw2"
})
@RestController
@RequestMapping("/api/OwnerParcels")
@RequiredArgsConstructor
public class ResidentParcelController {

    private final ParcelService parcelService;

    @GetMapping
    public List<ParcelListItemDto> getMyParcels() {
        return parcelService.getParcelsForCurrentResident();
    }

    @GetMapping("/{id}")
    public ParcelDetailDto getMyParcelDetail(@PathVariable Integer id) {
        return parcelService.getParcelDetailForResident(id);
    }

    @PostMapping("/{id}/confirm")
    public ParcelDetailDto confirmMyParcel(@PathVariable Integer id) {
        return parcelService.confirmParcelReceivedByResident(id);
    }
}
