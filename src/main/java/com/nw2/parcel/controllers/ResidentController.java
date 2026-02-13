package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.ResidentListDto;
import com.nw2.parcel.services.ResidentService;
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
@RequestMapping("/api/residents")
@RequiredArgsConstructor
public class ResidentController {

    private final ResidentService residentService;

    @GetMapping
    public List<ResidentListDto> getResidents() {
        return residentService.getAllResidents();
    }
}
