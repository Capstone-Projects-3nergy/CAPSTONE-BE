package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Announcement;
import com.nw2.parcel.entity.AnnouncementView;
import com.nw2.parcel.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnnouncementViewRepository extends JpaRepository<AnnouncementView, Integer> {
    boolean existsByAnnouncementAndUser(
            Announcement announcement,
            Users user
    );
}
