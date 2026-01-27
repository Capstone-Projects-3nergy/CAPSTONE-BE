package com.nw2.parcel.controllers;

import org.springframework.security.core.Authentication;
import com.nw2.parcel.Dtos.ManagementDetailDto;
import com.nw2.parcel.Dtos.ManagementListDto;
import com.nw2.parcel.Dtos.ManagementAddDto;
import com.nw2.parcel.services.ManagementService;
import com.nw2.parcel.services.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//management
@RestController
@RequestMapping("/api/staff/users")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://cp25nw2.sit.kmutt.ac.th",
        "https://cp25nw2.sit.kmutt.ac.th"
})
@RequiredArgsConstructor
public class ManagementController {

    private final ManagementService staffResidentService;
    private final TrashService trashService;

    //list table
    @GetMapping
    public List<ManagementListDto> getAllResidents() {
        return staffResidentService.getAllResidents();
    }

    //detail
    @GetMapping("/{id}")
    public ManagementDetailDto getDetail(@PathVariable Integer id) {
        return staffResidentService.getResidentDetail(id);
    }

    //add
    @PostMapping
    public ResponseEntity<?> createResident(@RequestBody ManagementAddDto req) {
        staffResidentService.addResident(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //edit
    @PutMapping("/{id}")
    public ResponseEntity<?> updateResident(
            @PathVariable Integer id,
            @RequestBody ManagementAddDto req
    ) {
        staffResidentService.updateResident(id, req);
        return ResponseEntity.ok().build();
    }

    // soft delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> moveResidentToTrash(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        String email = authentication.getName();
        staffResidentService.softDeleteResident(id, email);
        return ResponseEntity.noContent().build();
    }

}
