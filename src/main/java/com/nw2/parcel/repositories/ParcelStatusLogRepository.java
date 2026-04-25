package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.ParcelStatusLog;
import com.nw2.parcel.entity.Parcels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ParcelStatusLogRepository extends JpaRepository<ParcelStatusLog, Integer> {

    // ─── History ของพัสดุชิ้นเดียว ──────────────────────────────────────────────

    List<ParcelStatusLog> findByParcelParcelIdOrderByChangedAtAsc(Integer parcelId);

    // ─── Dashboard Queries ───────────────────────────────────────────────────────

    /**
     * นับจำนวนพัสดุที่ถูก PICKED_UP ต่อวัน (ย้อนหลังตาม from)
     * Return: List of [date (LocalDate), count (Long)]
     */
    @Query("""
            SELECT CAST(l.changedAt AS DATE), COUNT(l)
            FROM ParcelStatusLog l
            WHERE l.newStatus = com.nw2.parcel.entity.Parcels.Status.PICKED_UP
              AND l.changedAt >= :from
            GROUP BY CAST(l.changedAt AS DATE)
            ORDER BY CAST(l.changedAt AS DATE)
            """)
    List<Object[]> countPickedUpGroupByDate(@Param("from") LocalDateTime from);

    /**
     * นับจำนวนพัสดุที่เข้าใหม่ต่อวัน (oldStatus IS NULL = สร้างครั้งแรก)
     * Return: List of [date (LocalDate), count (Long)]
     */
    @Query("""
            SELECT CAST(l.changedAt AS DATE), COUNT(l)
            FROM ParcelStatusLog l
            WHERE l.newStatus IN (
                com.nw2.parcel.entity.Parcels.Status.WAITING,
                com.nw2.parcel.entity.Parcels.Status.WAITING_FOR_STAFF
            )
              AND l.oldStatus IS NULL
              AND l.changedAt >= :from
            GROUP BY CAST(l.changedAt AS DATE)
            ORDER BY CAST(l.changedAt AS DATE)
            """)
    List<Object[]> countNewParcelsGroupByDate(@Param("from") LocalDateTime from);

    /**
     * นับจำนวนพัสดุที่กลายเป็น OVERDUE ต่อวัน
     * Return: List of [date (LocalDate), count (Long)]
     */
    @Query("""
            SELECT CAST(l.changedAt AS DATE), COUNT(l)
            FROM ParcelStatusLog l
            WHERE l.newStatus = com.nw2.parcel.entity.Parcels.Status.OVERDUE
              AND l.changedAt >= :from
            GROUP BY CAST(l.changedAt AS DATE)
            ORDER BY CAST(l.changedAt AS DATE)
            """)
    List<Object[]> countOverdueGroupByDate(@Param("from") LocalDateTime from);

    /**
     * ดึง log ทั้งหมดในช่วงเวลาที่กำหนด (สำหรับ export / audit)
     */
    @Query("""
            SELECT l FROM ParcelStatusLog l
            WHERE l.changedAt BETWEEN :from AND :to
            ORDER BY l.changedAt DESC
            """)
    List<ParcelStatusLog> findAllInRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );

    /**
     * นับ status transition ทั้งหมดในช่วง (สำหรับ summary dashboard)
     * Return: List of [newStatus (String), count (Long)]
     */
    @Query("""
            SELECT l.newStatus, COUNT(l)
            FROM ParcelStatusLog l
            WHERE l.changedAt >= :from
            GROUP BY l.newStatus
            """)
    List<Object[]> countByNewStatusSince(@Param("from") LocalDateTime from);
}
