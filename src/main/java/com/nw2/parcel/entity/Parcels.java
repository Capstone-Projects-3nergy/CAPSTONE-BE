package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "parcels")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Parcels {

    public enum Status {
        PENDING, RECEIVED, PICKED_UP
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parcel_id")
    private Long parcelId;

    @Column(name = "tracking_number", nullable = false, length = 45)
    private String trackingNumber;

    @Column(name = "recipient_name", nullable = false, length = 45)
    private String recipientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "parcel_type", length = 45)
    private String parcelType;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(name = "sender_name", length = 20)
    private String senderName;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "companies_company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "users_user_id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL)
    private List<Notification> notifications;
}