package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff_dorms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffDorm {

    @EmbeddedId
    private StaffDormId id;

    @ManyToOne
    @MapsId("dormId")
    @JoinColumn(name = "dorm_id", nullable = false)
    private Dorm dorm;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}

