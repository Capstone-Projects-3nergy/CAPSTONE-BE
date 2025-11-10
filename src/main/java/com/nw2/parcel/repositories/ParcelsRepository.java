package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Parcels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ParcelsRepository extends JpaRepository<Parcels, Integer>, JpaSpecificationExecutor<Parcels> {}
