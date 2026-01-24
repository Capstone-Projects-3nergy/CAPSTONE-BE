package com.nw2.parcel.controllers;

import org.springframework.security.core.Authentication;
import com.nw2.parcel.Dtos.ResidentDetailDto;
import com.nw2.parcel.Dtos.ResidentListResponse;
import com.nw2.parcel.Dtos.CreateResidentDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.services.StaffResidentService;
import com.nw2.parcel.services.TrashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//management
@RestController
@RequestMapping("/api/staff/users")
@RequiredArgsConstructor
public class StaffResidentController {

    private final StaffResidentService staffResidentService;
    private final TrashService trashService;

    //list table
    @GetMapping
    public List<ResidentListResponse> getAllResidents() {
        return staffResidentService.getAllResidents();
    }

    //detail
    @GetMapping("/{id}")
    public ResidentDetailDto getDetail(@PathVariable Integer id) {
        return staffResidentService.getResidentDetail(id);
    }

    //add
    @PostMapping
    public ResponseEntity<?> createResident(@RequestBody CreateResidentDto req) {
        staffResidentService.addResident(req);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //edit
    @PutMapping("/{id}")
    public ResponseEntity<?> updateResident(
            @PathVariable Integer id,
            @RequestBody CreateResidentDto req
    ) {
        staffResidentService.updateResident(id, req);
        return ResponseEntity.ok().build();
    }

    //delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> moveResidentToTrash(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        Users staff = (Users) authentication.getPrincipal();
        trashService.deleteResident(id, staff);
        return ResponseEntity.noContent().build();
    }

}
