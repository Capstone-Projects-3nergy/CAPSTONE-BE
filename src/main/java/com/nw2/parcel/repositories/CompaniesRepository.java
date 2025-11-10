package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompaniesRepository extends JpaRepository<Company, Integer> {
    Optional<Company> findByCompanyNameIgnoreCase(String companyName);
}
