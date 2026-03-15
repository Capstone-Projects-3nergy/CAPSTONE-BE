package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "announcement_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    private String categoryName;
}