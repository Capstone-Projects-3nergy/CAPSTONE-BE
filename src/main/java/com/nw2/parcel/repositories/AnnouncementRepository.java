package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Integer> {

    List<Announcement> findByStatusAndDeletedAtIsNullOrderByIsPinnedDescPriorityDescPublishAtDesc(
            Announcement.Status status
    );
    List<Announcement> findByDeletedAtIsNotNullOrderByDeletedAtDesc();
    List<Announcement> findByStatusAndTargetAudienceAndDeletedAtIsNullOrderByIsPinnedDescPriorityDescPublishAtDesc(
            Announcement.Status status,
            Announcement.TargetAudience audience
    );
    List<Announcement> findByStatusAndPublishAtBefore(
            Announcement.Status status,
            LocalDateTime time
    );
}