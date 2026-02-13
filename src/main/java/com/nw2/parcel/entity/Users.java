package com.nw2.parcel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    public enum Role { RESIDENT, STAFF, ADMIN }
    public enum Status { PENDING, ACTIVE, INACTIVE, DELETED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

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

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "is_welcome_sent", nullable = false)
    private Boolean isWelcomeSent = false;

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

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(optional = true)
    @JsonIgnore
    @JoinColumn(name = "dorm_id", nullable = true)
    private Dorm dorm;

    @OneToMany(mappedBy = "user")
    private List<Parcels> parcels;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Notification> notifications;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<StaffDorm> staffDorms;

    @OneToMany(mappedBy = "deletedBy")
    @JsonIgnore
    private List<Trash> deletedTrashList;
}
