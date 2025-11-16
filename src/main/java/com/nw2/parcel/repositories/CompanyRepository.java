package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Integer> {
}