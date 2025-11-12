package com.nw2.parcel.repositories;

import com.nw2.parcel.Dtos.DormListDto;
import com.nw2.parcel.entity.Dorm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DormRepository extends JpaRepository<Dorm, Integer> {
    Optional<Dorm> findByDormName(String dormName);
    @Query("""
           SELECT new com.nw2.parcel.Dtos.DormListDto(d.dormId, d.dormName)
           FROM Dorm d
           ORDER BY d.dormName ASC
           """)
    List<DormListDto> findAllAsListDto();
}
