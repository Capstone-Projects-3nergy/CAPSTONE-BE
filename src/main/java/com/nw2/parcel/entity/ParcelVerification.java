package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "parcel_verifications", uniqueConstraints = @UniqueConstraint(columnNames = "tracking_number"))
@Getter
@Setter
@NoArgsConstructor
public class ParcelVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tracking_number", nullable = false, length = 100)
    private String trackingNumber;

    @Column(name = "resident_name", nullable = false, length = 100)
    private String residentName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private Users resident;

    @Column(name = "verified", nullable = false, columnDefinition = "TINYINT(1)"
    )
    private Boolean verified = false;

    @Column(name = "created_at", nullable = false, updatable = false
    )
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.verified == null) {
            this.verified = false;
        }
    }
}
