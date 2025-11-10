// com.nw2.parcel.services.ParcelSpecs.java
package com.nw2.parcel.services;

import com.nw2.parcel.entity.Parcels;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.JoinType;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ParcelSpecs {

    public static Specification<Parcels> keyword(String kw) {
        if (kw == null || kw.isBlank()) return (root, q, cb) -> cb.conjunction();

        final String like = "%" + kw.trim().toLowerCase() + "%";

        return (root, q, cb) -> {
            var user = root.join("user", JoinType.INNER);   // parcels.user
            return cb.or(
                    cb.like(cb.lower(root.get("trackingNumber")), like),
                    cb.like(cb.lower(root.get("recipientName")), like),
                    cb.like(cb.lower(user.get("firstName")), like),
                    cb.like(cb.lower(user.get("lastName")), like),
                    cb.like(cb.lower(user.get("roomNumber")), like)
            );
        };
    }

    public static Specification<Parcels> status(String status) {
        if (status == null || status.isBlank()) return (root, q, cb) -> cb.conjunction();
        return (root, q, cb) -> cb.equal(root.get("status"),
                Parcels.Status.valueOf(status.toUpperCase()));
    }

    public static Specification<Parcels> receivedOn(LocalDate day) {
        if (day == null) return (root, q, cb) -> cb.conjunction();
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end   = day.plusDays(1).atStartOfDay();
        return (root, q, cb) -> cb.between(root.get("receivedAt"), start, end);
    }

    // จำกัดให้เห็นเฉพาะพัสดุของหอที่กำหนด (ถ้าใช้สิทธิ์ตามหอ)
    public static Specification<Parcels> inDormIds(Iterable<Integer> dormIds) {
        if (dormIds == null) return (root, q, cb) -> cb.conjunction();
        return (root, q, cb) -> {
            var dorm = root.join("user", JoinType.INNER).join("dorm", JoinType.INNER);
            return dorm.get("dormId").in(dormIds);
        };
    }
}
