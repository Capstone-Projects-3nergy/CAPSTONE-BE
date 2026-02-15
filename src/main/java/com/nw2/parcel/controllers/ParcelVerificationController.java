package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.VerifyParcelDto;
import com.nw2.parcel.services.ParcelVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://cp25nw2.sit.kmutt.ac.th",
        "https://cp25nw2.sit.kmutt.ac.th",
        "http://cp25nw2.sit.kmutt.ac.th",
        "https://cp25nw2.sit.kmutt.ac.th"
})
@RestController
@RequestMapping("/api/resident/verify-parcel")
@RequiredArgsConstructor
public class ParcelVerificationController {

    private final ParcelVerificationService service;

    @PostMapping
    public void verifyParcel(@RequestBody VerifyParcelDto req) {
        service.verifyParcel(req);
    }
}
