package com.nw2.parcel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer announcementId;

    private String title;

    private String subtitle;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private TargetAudience targetAudience;

    private Boolean isPinned;

    private Integer priority;

    private Boolean sendNotification;

    private LocalDateTime publishAt;

    private Integer viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private Users createdBy;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private AnnouncementCategory category;

    @ManyToOne
    @JoinColumn(name = "dorm_id")
    private Dorm dorm;

    public enum Status {
        DRAFT,
        PUBLISHED
    }

    public enum TargetAudience {
        ALL_RESIDENTS,
        ACTIVE_ONLY
    }
}
