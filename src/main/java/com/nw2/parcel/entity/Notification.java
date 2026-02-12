package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    public enum Status { PENDING, SENT, FAILED, READ }
    public enum Type   { EMAIL, LINE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false)
    private Integer notificationId;

    @Column(name = "noti_title", nullable = false, length = 45)
    private String notiTitle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private Type notificationType;

    @Column(name = "noti_message", length = 300)
    private String notiMessage;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "parcel_id", nullable = false)
    private Parcels parcel;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
}
