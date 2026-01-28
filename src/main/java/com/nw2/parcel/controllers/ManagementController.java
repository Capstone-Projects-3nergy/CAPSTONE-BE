package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.ManagementDetailDto;
import com.nw2.parcel.Dtos.ManagementListDto;
import com.nw2.parcel.Dtos.ManagementAddDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.services.ManagementService;
import com.nw2.parcel.services.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

    @GetMapping
    public List<ManagementListDto> getAllResidents() {
        return staffResidentService.getAllResidents();
    }

    @GetMapping("/{id}")
    public ManagementDetailDto getDetail(@PathVariable Integer id) {
        return staffResidentService.getResidentDetail(id);
    }

    // ✅ รองรับ multipart/form-data
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Users> createResident(
            @RequestPart("data") ManagementAddDto req,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        Users savedUser = staffResidentService.addResident(req, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    // ✅ รองรับ multipart/form-data
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Users> updateResident(
            @PathVariable Integer id,
            @RequestPart("data") ManagementAddDto req,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        Users updatedUser = staffResidentService.updateResident(id, req, profileImage);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveResidentToTrash(@PathVariable Integer id) {
        staffResidentService.softDeleteResident(id);
    }

}