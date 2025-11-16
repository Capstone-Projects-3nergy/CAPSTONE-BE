package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.CreateParcelDto;
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
    public Parcels createParcel(@RequestBody CreateParcelDto req) {
        return parcelService.createParcel(req);
    }

    // ต่อไปจะมี GET /api/parcels, GET /api/parcels/{id}, PUT, PATCH /status ฯลฯ ได้อีก
}
