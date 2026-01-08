package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trash")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trash_id")
    private Integer trashId;

    // 🔗 One-to-One กับ Parcels
    @OneToOne
    @JoinColumn(name = "parcel_id", nullable = false, unique = true)
    private Parcels parcel;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    // 🔗 Many-to-One กับ Users (คนลบ)
    @ManyToOne
    @JoinColumn(name = "users_user_id", nullable = false)
    private Users deletedBy;
}