// com.nw2.parcel.entity.Parcels
package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name = "parcels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Parcels {

    public enum Status { PENDING, RECEIVED, PICKED_UP }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parcel_id", nullable = false)
    private Integer parcelId;

    @Column(name = "tracking_number", nullable = false, length = 100)
    private String trackingNumber;

    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "parcel_type", length = 45)
    private String parcelType;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "picked_up_at") // nullable ในสคีมา
    private LocalDateTime pickedUpAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private Users user; // เจ้าของพัสดุ = resident

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    @PrePersist
    void prePersist() {
        if (receivedAt == null) receivedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }
}
