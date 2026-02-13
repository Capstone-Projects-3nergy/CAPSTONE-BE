package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.ManagementDetailDto;
import com.nw2.parcel.Dtos.ManagementListDto;
import com.nw2.parcel.Dtos.ManagementAddDto;
import com.nw2.parcel.Dtos.ManagementUpdateDto;
import com.nw2.parcel.services.ManagementService;
import com.nw2.parcel.services.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://cp25nw2.sit.kmutt.ac.th",
        "https://cp25nw2.sit.kmutt.ac.th",
        "http://cp25nw2.sit.kmutt.ac.th/capstone25/cp25nw2",
        "https://cp25nw2.sit.kmutt.ac.th/capstone25/cp25nw2"
})
@RestController
@RequestMapping("/api/staff/users")
@RequiredArgsConstructor
public class ManagementController {

    private final ManagementService staffResidentService;
    private final TrashService trashService;
    private final ManagementService managementService;

    //view
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
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ManagementDetailDto> createResident(
            @RequestPart("data") ManagementAddDto req,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        ManagementDetailDto savedUser = staffResidentService.addResident(req, profileImage);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    //edit
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ManagementDetailDto> updateResident(
            @PathVariable Integer id,
            @RequestPart("data") ManagementUpdateDto req,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        ManagementDetailDto updatedUser = staffResidentService.updateResident(id, req, profileImage);
        return ResponseEntity.ok(updatedUser);
    }

    //delete
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveResidentToTrash(@PathVariable Integer id) {
        staffResidentService.softDeleteResident(id);
    }

}