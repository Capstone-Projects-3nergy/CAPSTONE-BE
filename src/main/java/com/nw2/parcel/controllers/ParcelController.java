package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.*;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.services.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/parcels")
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;
    private final ParcelService parcelMetaService;

    //add
    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public ParcelDto createParcel(@RequestBody CreateParcelDto req) {
        Parcels p = parcelService.createParcel(req);

        return new ParcelDto(
                p.getParcelId(),
                p.getTrackingNumber(),
                p.getRecipientName(),
                p.getStatus(),
                p.getParcelType(),
                p.getSenderName(),
                p.getCompany() != null ? p.getCompany().getCompanyId() : null,
                p.getCompany() != null ? p.getCompany().getCompanyName() : null,
                p.getUser() != null ? p.getUser().getUserId() : null
        );
    }

    //view
    @GetMapping
    public List<ParcelListItemDto> getAllParcelsForStaff() {
        return parcelService.getAllParcelsForStaff();
    }

    //details
    @GetMapping("/{id}")
    public ParcelDetailDto getParcelDetail(@PathVariable Integer id) {
        return parcelService.getParcelDetail(id);
    }

    //edit
    @PutMapping("/{id}")
    public ParcelDetailDto updateParcel(
            @PathVariable Integer id,
            @RequestBody UpdateParcelDto req
    ) {
        return parcelService.updateParcelForStaff(id, req);
    }

    // soft delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveParcelToTrash(@PathVariable Integer id) {
        parcelService.moveParcelToTrash(id);
    }

    @GetMapping("/types")
    public List<String> getParcelTypes() {
        return parcelMetaService.getParcelTypes();
    }
}
