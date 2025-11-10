// src/main/java/com/nw2/parcel/controllers/ParcelController.java
package com.nw2.parcel.controllers;

import com.google.firebase.auth.FirebaseToken;
import com.nw2.parcel.Dtos.*;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.services.ParcelService;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/parcels")
@CrossOrigin(origins = {"http://localhost:5173","http://127.0.0.1:5173", "http://bscit.sit.kmutt.ac.th","https://bscit.sit.kmutt.ac.th"})
public class ParcelController {

    private final ParcelService parcelService;
    private final UsersRepository usersRepository;

    public ParcelController(ParcelService parcelService, UsersRepository usersRepository) {
        this.parcelService = parcelService;
        this.usersRepository = usersRepository;
    }

    // ---------- ADD ----------
    @PostMapping("/add")
    public ResponseEntity<?> addParcel(@RequestBody AddParcelRequest req, Authentication auth) {
        FirebaseToken tok = (FirebaseToken) auth.getDetails();
        Users staffUser = usersRepository.findByEmail(tok.getEmail())
                .orElseThrow(() -> new RuntimeException("Staff not found"));
        Parcels parcel = parcelService.addParcel(req, staffUser);
        return ResponseEntity.ok("Parcel added successfully (id=" + parcel.getParcelId() + ")");
    }

    // ---------- LIST (สำหรับตาราง) ----------
    @GetMapping
    public ResponseEntity<PageResponse<ParcelListItemDto>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String status, // PENDING/RECEIVED/PICKED_UP
            @RequestParam(required = false) String day,   // รูปแบบ YYYY-MM-DD จากแท็บ Day
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "receivedAt,desc") String sort
    ) {
        Sort sortBy = Sort.by(sort.split(",")[0]);
        if (sort.toLowerCase().endsWith(",asc")) sortBy = sortBy.ascending(); else sortBy = sortBy.descending();

        LocalDateTime dayStart = null, dayEnd = null;
        if (day != null && !day.isBlank()) {
            LocalDate d = LocalDate.parse(day, DateTimeFormatter.ISO_DATE);
            dayStart = d.atStartOfDay();
            dayEnd = d.plusDays(1).atStartOfDay();
        }

        return ResponseEntity.ok(
                parcelService.listParcels(q, status, dayStart, dayEnd, page, size, sortBy)
        );
    }

    // ---------- DETAIL (โหลดข้อมูลไปหน้าแก้ไข) ----------
    @GetMapping("/{id}")
    public ResponseEntity<ParcelDetailDto> detail(@PathVariable Integer id) {
        return ResponseEntity.ok(parcelService.getParcelDetail(id));
    }

    // ---------- EDIT (ยกเว้น trackingNumber) ----------
    @PutMapping("/{id}")
    public ResponseEntity<ParcelDetailDto> edit(@PathVariable Integer id, @RequestBody EditParcelRequest req) {
        // บังคับ id จาก path
        EditParcelRequest fixed = new EditParcelRequest(
                id, req.status(), req.pickedUpAt(),
                req.parcelType(), req.senderName(), req.imageUrl(),
                req.companyId(), req.companyName()
        );
        Parcels p = parcelService.updateParcel(fixed);
        // ส่งรายละเอียดล่าสุดกลับให้หน้าแก้ไขรีเฟรช
        return ResponseEntity.ok(parcelService.getParcelDetail(p.getParcelId()));
    }
}
