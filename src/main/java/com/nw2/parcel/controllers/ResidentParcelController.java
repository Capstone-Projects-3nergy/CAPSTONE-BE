package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.ParcelDetailDto;
import com.nw2.parcel.Dtos.ParcelListItemDto;
import com.nw2.parcel.services.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/OwnerParcels")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://bscit.sit.kmutt.ac.th",
        "https://bscit.sit.kmutt.ac.th"
})
@RequiredArgsConstructor
public class ResidentParcelController {

    private final ParcelService parcelService;

    // (extra) list ของ resident เอง
    @GetMapping
    public List<ParcelListItemDto> getMyParcels() {
        return parcelService.getParcelsForCurrentResident();
    }

    // 📌 VIEW-PARCEL-DETAIL (resident)
    @GetMapping("/{id}")
    public ParcelDetailDto getMyParcelDetail(@PathVariable Integer id) {
        return parcelService.getParcelDetailForResident(id);
    }

    // 📌 CONFIRM-RECEIVED-PARCEL (resident กด confirm รับพัสดุแล้ว)
    @PostMapping("/{id}/confirm")
    public ParcelDetailDto confirmMyParcel(@PathVariable Integer id) {
        return parcelService.confirmParcelReceivedByResident(id);
    }
}
