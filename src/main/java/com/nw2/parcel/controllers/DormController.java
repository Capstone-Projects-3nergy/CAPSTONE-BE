package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.DormListDto;
import com.nw2.parcel.entity.Dorm;
import com.nw2.parcel.repositories.DormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2"
})
@RestController
@RequestMapping("/api/dorms")
@RequiredArgsConstructor
public class DormController {

    private final DormRepository dormRepository;

    @GetMapping
    public List<Dorm> getAllDorms() {
        return dormRepository.findAll();
    }

    @GetMapping("/{id}")
    public Dorm getDormById(@PathVariable Integer id) {
        return dormRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dorm not found: " + id));
    }

    @GetMapping("/list")
    public List<DormListDto> listDormsForSelect() {
        return dormRepository.findAllAsListDto();
    }

    @GetMapping("/search")
    public Dorm getDormByName(@RequestParam("name") String dormName) {
        return dormRepository.findByDormName(dormName)
                .orElseThrow(() -> new RuntimeException("Dorm not found: " + dormName));
    }
}

