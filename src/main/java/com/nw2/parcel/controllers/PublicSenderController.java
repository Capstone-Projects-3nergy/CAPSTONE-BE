package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.ParcelDto;
import com.nw2.parcel.Dtos.SenderCreateParcelDto;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.services.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/parcels")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://bscit.sit.kmutt.ac.th",
        "https://bscit.sit.kmutt.ac.th"
})
@RequiredArgsConstructor
public class PublicSenderController {
    private final ParcelService parcelService;

    // คนส่งใช้ endpoint นี้
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ParcelDto createParcelFromPublic(@RequestBody SenderCreateParcelDto req) {
        Parcels p = parcelService.createParcelFromPublicForm(req);

        return new ParcelDto(
                p.getParcelId(),
                p.getTrackingNumber(),
                p.getRecipientName(),
                p.getStatus(),
                p.getParcelType(),
                p.getSenderName(),
                p.getCompany() != null ? p.getCompany().getCompanyId() : null,
                p.getCompany() != null ? p.getCompany().getCompanyName() : null,
                p.getUser() != null ? p.getUser().getUserId() : null   // น่าจะเป็น null ในเคส public
        );
    }
}
