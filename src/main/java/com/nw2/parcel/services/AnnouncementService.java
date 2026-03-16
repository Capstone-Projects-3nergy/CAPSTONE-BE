package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.AnnouncementCategoryDto;
import com.nw2.parcel.Dtos.AnnouncementDto;
import com.nw2.parcel.Dtos.CreateAnnouncementDto;
import com.nw2.parcel.Dtos.UpdateAnnouncementDto;
import com.nw2.parcel.entity.*;
import com.nw2.parcel.exception.ResourceNotFoundException;
import com.nw2.parcel.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementCategoryRepository categoryRepository;
    private final UsersRepository usersRepository;
    private final AnnouncementViewRepository viewRepository;
    private final TrashRepository trashRepository;
    private final NotificationService notificationService;

    // LIST ANNOUNCEMENTS
    public List<AnnouncementDto> getAllPublished() {

        return announcementRepository
                .findByStatusAndDeletedAtIsNullOrderByIsPinnedDescPriorityDescPublishAtDesc(
                        Announcement.Status.PUBLISHED
                )
                .stream()
                .map(this::map)
                .toList();
    }

    // GET ANNOUNCEMENT BY ID
    public AnnouncementDto getById(Integer id) {

        Announcement ann = announcementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        if (ann.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Announcement not found");
        }

        if (ann.getStatus() != Announcement.Status.PUBLISHED) {
            throw new ResourceNotFoundException("Announcement not found");
        }

        return map(ann);
    }

    // CREATE ANNOUNCEMENT
    public AnnouncementDto createAnnouncement(
            CreateAnnouncementDto req,
            Users staff
    ) {

        AnnouncementCategory category =
                categoryRepository.findById(req.getCategoryId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Category not found"));

        LocalDateTime now = LocalDateTime.now();

        Announcement.Status status;
        LocalDateTime publishTime;

        if (Boolean.TRUE.equals(req.getPublishNow())) {

            status = Announcement.Status.PUBLISHED;
            publishTime = now;

        } else if (req.getPublishAt() != null && req.getPublishAt().isAfter(now)) {

            status = Announcement.Status.DRAFT;
            publishTime = req.getPublishAt();

        } else {

            status = Announcement.Status.DRAFT;
            publishTime = null;
        }

        Announcement announcement = Announcement.builder()
                .title(req.getTitle())
                .subtitle(req.getSubtitle())
                .content(req.getContent())
                .coverImageUrl(req.getCoverImageUrl())
                .category(category)
                .createdBy(staff)
                .isPinned(req.getPinned())
                .priority(req.getPriority())
                .sendNotification(req.getSendNotification())
                .publishAt(publishTime)
                .viewCount(0)
                .status(status)
                .targetAudience(Announcement.TargetAudience.ALL_RESIDENTS)
                .createdAt(now)
                .build();

        Announcement saved = announcementRepository.save(announcement);

        // SEND NOTIFICATION
        if (status == Announcement.Status.PUBLISHED &&
                Boolean.TRUE.equals(saved.getSendNotification())) {

            List<Users> residents =
                    usersRepository.findByRole(Users.Role.RESIDENT);

            for (Users user : residents) {

                notificationService.notifyAnnouncement(
                        user,
                        saved.getTitle(),
                        saved.getSubtitle()
                );
            }
        }

        return map(saved);
    }

    // UPDATE ANNOUNCEMENT
    public AnnouncementDto updateAnnouncement(
            Integer id,
            UpdateAnnouncementDto req
    ) {

        Announcement ann = announcementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        if (req.getTitle() != null)
            ann.setTitle(req.getTitle());

        if (req.getSubtitle() != null)
            ann.setSubtitle(req.getSubtitle());

        if (req.getContent() != null)
            ann.setContent(req.getContent());

        if (req.getCoverImageUrl() != null)
            ann.setCoverImageUrl(req.getCoverImageUrl());

        if (req.getPinned() != null)
            ann.setIsPinned(req.getPinned());

        if (req.getPriority() != null)
            ann.setPriority(req.getPriority());

        if (req.getPublishAt() != null)
            ann.setPublishAt(req.getPublishAt());

        ann.setUpdatedAt(LocalDateTime.now());

        return map(announcementRepository.save(ann));
    }

    // MOVE TO TRASH
    public void moveToTrash(Integer id, Users deletedBy) {

        Announcement ann = announcementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        ann.setDeletedAt(LocalDateTime.now());
        announcementRepository.save(ann);

        Trash trash = Trash.builder()
                .targetType(Trash.TargetType.ANNOUNCEMENT)
                .targetId(id)
                .deletedBy(deletedBy)
                .deletedAt(LocalDateTime.now())
                .build();

        trashRepository.save(trash);
    }

    // RECORD VIEW
    public void recordView(Integer announcementId, Users user) {

        Announcement ann = announcementRepository
                .findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        boolean alreadyViewed =
                viewRepository.existsByAnnouncementAndUser(ann, user);

        if (!alreadyViewed) {

            AnnouncementView view = AnnouncementView.builder()
                    .announcement(ann)
                    .user(user)
                    .viewedAt(LocalDateTime.now())
                    .build();

            viewRepository.save(view);

            Integer current = ann.getViewCount() == null ? 0 : ann.getViewCount();
            ann.setViewCount(current + 1);

            announcementRepository.save(ann);
        }
    }

    private AnnouncementDto map(Announcement a) {

        return AnnouncementDto.builder()
                .id(a.getAnnouncementId())
                .title(a.getTitle())
                .subtitle(a.getSubtitle())
                .content(a.getContent())
                .coverImageUrl(a.getCoverImageUrl())
                .category(
                        a.getCategory() != null
                                ? a.getCategory().getCategoryName()
                                : null
                )
                .pinned(a.getIsPinned())
                .priority(a.getPriority())
                .publishAt(a.getPublishAt())
                .viewCount(a.getViewCount())
                .status(a.getStatus().name())
                .build();
    }

    // LIST CATEGORIES
    public List<AnnouncementCategoryDto> getAllCategories() {

        return categoryRepository
                .findAll()
                .stream()
                .map(c -> new AnnouncementCategoryDto(
                        c.getCategoryId(),
                        c.getCategoryName()
                ))
                .toList();
    }

    // LIST ALL (STAFF VIEW)
    public List<AnnouncementDto> getAllForStaff() {

        return announcementRepository
                .findByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(this::map)
                .toList();
    }
}