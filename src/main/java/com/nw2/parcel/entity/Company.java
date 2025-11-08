package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)   // ⬅ เพิ่ม
    @Column(name = "company_id", nullable = false)
    private Integer companyId;                            // ⬅ เปลี่ยน Long -> Integer

    @Column(name = "company_name", nullable = false, length = 30)
    private String companyName;

    @Column(name = "tracking_url", length = 100)
    private String trackingUrl;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<Parcels> parcels;
}

