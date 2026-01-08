package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Trash;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrashRepository extends JpaRepository<Trash, Integer> {

    Optional<Trash> findByParcelParcelId(Integer parcelId);

    void deleteByParcelParcelId(Integer parcelId);
}
