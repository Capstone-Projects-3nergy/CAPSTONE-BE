package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.VerifyParcelDto;
import com.nw2.parcel.services.ParcelVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2"
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
