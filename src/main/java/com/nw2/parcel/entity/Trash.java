package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trash")
@Data
public class Trash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer trashId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "deleted_by", nullable = false)
    private Users deletedBy;

    public enum TargetType {
        PARCEL,
        USER
    }
}
