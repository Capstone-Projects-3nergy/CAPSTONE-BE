package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.CompanyDto;
import com.nw2.parcel.entity.Company;
import com.nw2.parcel.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://bscit.sit.kmutt.ac.th",
        "https://bscit.sit.kmutt.ac.th"
})
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyRepository companyRepository;

    // ✅ ดึง list บริษัททั้งหมด (เอาไว้ populate dropdown)
    @GetMapping
    public List<CompanyDto> getAll() {
        return companyRepository.findAll().stream()
                .map(c -> new CompanyDto(
                        c.getCompanyId(),
                        c.getCompanyName()
//                        c.getTrackingUrl()
                ))
                .toList();
    }

    // (ถ้าอยากมีดึงทีละตัว)
    @GetMapping("/{id}")
    public CompanyDto getCompanyById(@PathVariable Integer id) {
        Company c = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found: " + id));

        return new CompanyDto(
                c.getCompanyId(),
                c.getCompanyName()
//                c.getTrackingUrl()
        );
    }
}