package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.VerifyParcelDto;
import com.nw2.parcel.services.ParcelVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
