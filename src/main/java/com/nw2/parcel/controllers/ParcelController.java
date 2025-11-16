package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.CreateParcelDto;
import com.nw2.parcel.Dtos.ParcelDto;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.services.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parcels")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://bscit.sit.kmutt.ac.th",
        "https://bscit.sit.kmutt.ac.th"
})
@RequiredArgsConstructor
public class ParcelController {

    private final ParcelService parcelService;

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

    // ต่อไปจะมี GET /api/parcels, GET /api/parcels/{id}, PUT, PATCH /status ฯลฯ ได้อีก
}
