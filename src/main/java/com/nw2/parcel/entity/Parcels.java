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

    public enum Status { PENDING, RECEIVED, PICKED_UP }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ⬅ เพิ่ม
    @Column(name = "parcel_id", nullable = false)
    private Integer parcelId;                            // ⬅ เปลี่ยน Long -> Integer

    @Column(name = "tracking_number", nullable = false, length = 45)
    private String trackingNumber;

    @Column(name = "recipient_name", nullable = false, length = 45)
    private String recipientName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "parcel_type", length = 45)
    private String parcelType;

    @Column(name = "image_url", length = 300)
    private String imageUrl;

    @Column(name = "sender_name", length = 20)
    private String senderName;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "picked_up_at", nullable = false)
    private LocalDateTime pickedUpAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL)
    private List<Notification> notifications;
}
