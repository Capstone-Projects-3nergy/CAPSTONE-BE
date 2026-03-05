package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.CompanyDto;
import com.nw2.parcel.entity.Company;
import com.nw2.parcel.repositories.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2"
})
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyRepository companyRepository;

    @GetMapping
    public List<CompanyDto> getAll() {
        return companyRepository.findAll().stream()
                .map(c -> new CompanyDto(
                        c.getCompanyId(),
                        c.getCompanyName()
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
        );
    }
}