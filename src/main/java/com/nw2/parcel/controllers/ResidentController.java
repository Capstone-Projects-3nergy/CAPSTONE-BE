package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.ResidentListDto;
import com.nw2.parcel.services.ResidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/residents")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://bscit.sit.kmutt.ac.th",
        "https://bscit.sit.kmutt.ac.th"
})
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    @GetMapping
    public List<ResidentListDto> getResidents() {
        return residentService.getAllResidents();
    }
}
