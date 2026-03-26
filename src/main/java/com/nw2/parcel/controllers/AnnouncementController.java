package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.AnnouncementCategoryDto;
import com.nw2.parcel.Dtos.AnnouncementDto;
import com.nw2.parcel.Dtos.CreateAnnouncementDto;
import com.nw2.parcel.Dtos.UpdateAnnouncementDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.services.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2"
})
@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;
    private final UsersRepository usersRepository;

    private Users getCurrentUser(Authentication authentication) {
        String firebaseUid = authentication.getName();
        return usersRepository
                .findByFirebaseUid(firebaseUid)
                .orElseThrow();
    }

    // ✅ CREATE (รองรับ upload รูป)
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public AnnouncementDto create(
            @RequestPart("data") CreateAnnouncementDto req,
            @RequestPart(value = "image", required = false) MultipartFile image,
            Authentication auth
    ) {
        Users staff = getCurrentUser(auth);
        return announcementService.createAnnouncement(req, staff, image);
    }

    // LIST
    @GetMapping
    public List<AnnouncementDto> getAll() {
        return announcementService.getAllPublished();
    }

    // DETAIL
    @GetMapping("/{id}")
    public AnnouncementDto getOne(@PathVariable Integer id) {
        return announcementService.getById(id);
    }

    // ✅ UPDATE (รองรับเปลี่ยนรูป)
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public AnnouncementDto update(
            @PathVariable Integer id,
            @RequestPart("data") UpdateAnnouncementDto req,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return announcementService.updateAnnouncement(id, req, image);
    }

    // MOVE TO TRASH
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void moveToTrash(
            @PathVariable Integer id,
            Authentication auth
    ) {
        Users staff = getCurrentUser(auth);
        announcementService.moveToTrash(id, staff);
    }

    // RECORD VIEW
    @PostMapping("/{id}/view")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recordView(
            @PathVariable Integer id,
            Authentication auth
    ) {
        Users user = getCurrentUser(auth);
        announcementService.recordView(id, user);
    }

    // list category
    @GetMapping("/categories")
    public List<AnnouncementCategoryDto> getCategories() {
        return announcementService.getAllCategories();
    }

    // STAFF VIEW
    @GetMapping("/staff")
    public List<AnnouncementDto> getAllForStaff() {
        return announcementService.getAllForStaff();
    }

    //staff edit draft
    @GetMapping("/staff/{id}")
    public AnnouncementDto getOneForStaff(@PathVariable Integer id) {
        return announcementService.getByIdForStaff(id);
    }
}