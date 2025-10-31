package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Dorm;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DormRepository extends JpaRepository<Dorm, Long> {
    // เพิ่มเมธอดค้นหาเองได้ เช่น
    Optional<Dorm> findByDormName(String dormName);
}
