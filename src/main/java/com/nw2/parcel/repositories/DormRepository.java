package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Dorm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DormRepository extends JpaRepository<Dorm, Integer> {
//    // ค้นหาแบบ exact match (ignore case)
//    List<Dorm> findByDormTypeIgnoreCase(String dormType);
//
//    // ค้นหาแบบ contains (มีคำว่า "female" หรือ "male" ในชื่อ)
//    List<Dorm> findByDormTypeContainingIgnoreCase(String dormType);
//
//    // ค้นหาด้วยชื่อหอ
//    Optional<Dorm> findByDormNameIgnoreCase(String dormName);
// ✅ ค้นหาด้วย Enum โดยตรง (แนะนำ)
List<Dorm> findByDormType(Dorm.DormType dormType);

    // ค้นหาด้วยชื่อหอ
    Optional<Dorm> findByDormNameIgnoreCase(String dormName);
}
