// com.nw2.parcel.controllers.DormController
package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.DormListItemDto;
import com.nw2.parcel.entity.Dorm;
import com.nw2.parcel.repositories.DormRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {
        "*",
        "http://localhost:5173",
        "http://127.0.0.1:5173", "http://bscit.sit.kmutt.ac.th",
        "https://bscit.sit.kmutt.ac.th"
})
public class DormController {

    private final DormRepository dormRepository;

    public DormController(DormRepository dormRepository) {
        this.dormRepository = dormRepository;
    }

    /** GET /api/public/dorms -> คืน id + name ทั้งหมด */
    @GetMapping("/dorms")
    public ResponseEntity<List<DormListItemDto>> listDorms() {
        List<DormListItemDto> result = dormRepository.findAll().stream()
                .map(d -> new DormListItemDto(d.getDormId(), d.getDormName()))
                .toList();
        return ResponseEntity.ok(result);
    }
}
