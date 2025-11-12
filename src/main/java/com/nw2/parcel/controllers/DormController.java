package com.nw2.parcel.controllers;

import com.nw2.parcel.entity.Dorm;
import com.nw2.parcel.repositories.DormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://bscit.sit.kmutt.ac.th",
        "https://bscit.sit.kmutt.ac.th" })
@RestController
@RequestMapping("/api/dorms")   // ⬅ เปลี่ยนเป็น /api/dorms
@RequiredArgsConstructor
public class DormController {

    private final DormRepository dormRepository;

    @GetMapping
    public List<Dorm> getAllDorms() {
        return dormRepository.findAll();
    }

    @GetMapping("/{dormName}")
    public Dorm getDormByName(@PathVariable String dormName) {
        return dormRepository.findByDormName(dormName)
                .orElseThrow(() -> new RuntimeException("Dorm not found: " + dormName));
    }
}

