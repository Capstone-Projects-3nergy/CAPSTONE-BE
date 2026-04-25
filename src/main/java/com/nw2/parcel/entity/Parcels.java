// com.nw2.parcel.entity.Parcels
package com.nw2.parcel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity @Table(name = "parcels")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Parcels {

    public enum Status { WAITING_FOR_STAFF,WAITING, PICKED_UP, OVERDUE }
    public enum Parceltype {BOX, DOCUMENT, ELECTRONIC}

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
    private Status status = Status.WAITING;

    @Enumerated(EnumType.STRING)
    @Column(name = "parcel_type")
    private Parceltype parcelType = Parceltype.BOX;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "sender_name", length = 100)
    private String senderName;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(optional = false)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id",nullable = true)
    private Users user;

    @OneToMany(mappedBy = "parcel", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Notification> notifications;

    @PrePersist
    void prePersist() {
        if (receivedAt == null) receivedAt = LocalDateTime.now();
        if (isDeleted == null) isDeleted = false;
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }
}
