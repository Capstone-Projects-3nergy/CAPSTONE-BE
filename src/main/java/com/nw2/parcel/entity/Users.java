package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {

    public enum Role {
        RESIDENT, STAFF, ADMIN
    }

    public enum Status {
        ACTIVE, INACTIVE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false)
    private long userId;

    @Column(name="firebase_uid", unique = true, length = 45)
    private String firebaseUid;

    @Column(name = "email", nullable = false, unique = true, length = 128)
    private String email;

    @Column(name = "first_name", length = 45)
    private String firstName;

    @Column(name = "last_name", length = 45)
    private String lastName;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "profile_image_url", length = 300)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Column(name = "position", length = 45)
    private String position;

    @Column(name = "line_id", length = 45)
    private String lineId;

    @Column(name = "room_number", length = 45)
    private String roomNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne
    @JoinColumn(name = "dorm_id", referencedColumnName = "dorm_id")
    private Dorm dorm;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Parcels> parcels;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<StaffDorm> staffDorms;
}