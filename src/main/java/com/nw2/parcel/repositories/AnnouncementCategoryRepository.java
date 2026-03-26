package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.AnnouncementCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementCategoryRepository
        extends JpaRepository<AnnouncementCategory, Integer> {
}
