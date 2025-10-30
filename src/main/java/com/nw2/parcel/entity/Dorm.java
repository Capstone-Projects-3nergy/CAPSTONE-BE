package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "dorms")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dorm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dorm_id")
    private Long dormId;

    @Column(name = "dorm_name", nullable = false, length = 100)
    private String dormName;

    @Column(name = "address", nullable = false, length = 80)
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "dorm_type", nullable = false, length = 15)
    private String dormType;

    @Column(name = "email", length = 45)
    private String email;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "dorm", cascade = CascadeType.ALL)
    private List<Users> users;

    @OneToMany(mappedBy = "dorm", cascade = CascadeType.ALL)
    private List<StaffDorm> staffDorms;
}
