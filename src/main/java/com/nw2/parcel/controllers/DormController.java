//package com.nw2.parcel.controllers;
//
//import com.nw2.parcel.Dtos.DormListItemDto;
//import com.nw2.parcel.entity.Dorm;
//import com.nw2.parcel.repositories.DormRepository;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/public")
//@CrossOrigin(origins = { "*", "http://localhost:5173", "http://127.0.0.1:5173" })
//public class DormController {
//
//    private final DormRepository dormRepository;
//
//    public DormController(DormRepository dormRepository) {
//        this.dormRepository = dormRepository;
//    }
//
//    /**
//     * GET /public/dorms
//     * ตัวอย่าง:
//     *   /public/dorms                -> ทั้งหมด
//     *   /public/dorms?type=female    -> เฉพาะหอหญิง
//     *   /public/dorms?type=male      -> เฉพาะหอชาย
//     */
//    @GetMapping("/dorms")
//    public ResponseEntity<List<DormListItemDto>> listDorms(
//            @RequestParam(name = "type", required = false) String type) {
//
//        List<Dorm> dorms;
//        if (type == null || type.isBlank()) {
//            dorms = dormRepository.findAll();
//        } else {
//            // map ค่าจาก UI ให้ตรงกับข้อมูลใน DB
//            String normalized = type.trim().toLowerCase();
//            // ถ้าใน DB เก็บ "Female Dormitory" / "Male Dormitory" ให้ map เป็นค่าเดียวกัน
//            if (normalized.startsWith("female")) {
//                dorms = dormRepository.findByDormTypeIgnoreCase("Female Dormitory");
//            } else if (normalized.startsWith("male")) {
//                dorms = dormRepository.findByDormTypeIgnoreCase("Male Dormitory");
//            } else {
//                dorms = dormRepository.findByDormTypeIgnoreCase(type);
//            }
//        }
//
//        List<DormListItemDto> result = dorms.stream()
//                .map(d -> new DormListItemDto(d.getDormId(), d.getDormName()))
//                .toList();
//
//        return ResponseEntity.ok(result);
//    }
//}
package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.DormListItemDto;
import com.nw2.parcel.entity.Dorm;
import com.nw2.parcel.entity.Dorm.DormType;
import com.nw2.parcel.repositories.DormRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = { "*", "http://localhost:5173", "http://127.0.0.1:5173" })
public class DormController {

    private final DormRepository dormRepository;

    public DormController(DormRepository dormRepository) {
        this.dormRepository = dormRepository;
    }

    /**
     * GET /public/dorms
     * ตัวอย่าง:
     *   /public/dorms                -> ทั้งหมด
     *   /public/dorms?type=female    -> เฉพาะหอหญิง
     *   /public/dorms?type=male      -> เฉพาะหอชาย
     */
    @GetMapping("/dorms")
    public ResponseEntity<List<DormListItemDto>> listDorms(
            @RequestParam(name = "type", required = false) String type) {

        System.out.println("🔍 Received type parameter: " + type);

        List<Dorm> dorms;

        if (type == null || type.isBlank()) {
            // ไม่มี filter -> คืนทั้งหมด
            dorms = dormRepository.findAll();
            System.out.println("📊 Fetching all dorms, found: " + dorms.size());
        } else {
            // แปลง string เป็น enum
            String normalized = type.trim().toLowerCase();
            DormType dormType = null;

            if (normalized.equals("female") || normalized.startsWith("female")) {
                dormType = DormType.Female_Dormitory;
            } else if (normalized.equals("male") || normalized.startsWith("male")) {
                dormType = DormType.Male_Dormitory;
            }

            if (dormType != null) {
                dorms = dormRepository.findByDormType(dormType);
                System.out.println("🔎 Fetching " + dormType + " dorms, found: " + dorms.size());
            } else {
                System.out.println("⚠️ Invalid dorm type: " + type);
                dorms = List.of(); // คืน empty list
            }
        }

        // แปลงเป็น DTO
        List<DormListItemDto> result = dorms.stream()
                .map(d -> {
                    System.out.println("  ✅ " + d.getDormName() +
                            " (ID: " + d.getDormId() +
                            ", Type: " + d.getDormType() + ")");
                    return new DormListItemDto(d.getDormId(), d.getDormName());
                })
                .toList();

        System.out.println("📤 Returning " + result.size() + " dorms");
        return ResponseEntity.ok(result);
    }
}