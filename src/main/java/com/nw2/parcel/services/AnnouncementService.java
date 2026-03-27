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
import org.springframework.web.multipart.MultipartFile;

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
    private final FileStorageService fileStorageService;

    private static final int MAX_PINNED = 3;

    //PIN
    private void handlePinLogic(Announcement ann, Boolean requestPin) {

        if (requestPin == null) return;

        if (requestPin) {

            long pinnedCount = announcementRepository.countByIsPinnedTrue();

            if (pinnedCount >= MAX_PINNED) {

                List<Announcement> pinnedList =
                        announcementRepository.findByIsPinnedTrueOrderByPriorityAsc();

                Announcement lowest = pinnedList.get(0);
                lowest.setIsPinned(false);
                lowest.setPriority(0);
                announcementRepository.save(lowest);
            }

            Integer maxPriority = announcementRepository
                    .findByIsPinnedTrueOrderByPriorityAsc()
                    .stream()
                    .map(a -> a.getPriority() == null ? 0 : a.getPriority())
                    .max(Integer::compareTo)
                    .orElse(0);

            ann.setIsPinned(true);
            ann.setPriority(maxPriority + 1);

        } else {
            ann.setIsPinned(false);
            ann.setPriority(0);
        }
    }

    //NOTIFICATION HELPER
    private void sendAnnouncementNotification(Announcement ann) {

        List<Users> residents =
                usersRepository.findByRole(Users.Role.RESIDENT);

        for (Users user : residents) {
            notificationService.notifyAnnouncement(
                    user,
                    ann.getTitle(),
                    ann.getSubtitle()
            );
        }
    }

    // LIST
    public List<AnnouncementDto> getAllPublished() {

        return announcementRepository
                .findByStatusAndDeletedAtIsNullOrderByIsPinnedDescPriorityDescPublishAtDesc(
                        Announcement.Status.PUBLISHED
                )
                .stream()
                .map(this::map)
                .toList();
    }

    public AnnouncementDto getById(Integer id) {

        Announcement ann = announcementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        if (ann.getDeletedAt() != null ||
                ann.getStatus() != Announcement.Status.PUBLISHED) {
            throw new ResourceNotFoundException("Announcement not found");
        }

        return map(ann);
    }

    public AnnouncementDto createAnnouncement(
            CreateAnnouncementDto req,
            Users staff,
            MultipartFile image
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

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileStorageService.store(image);
        }

        Announcement announcement = Announcement.builder()
                .title(req.getTitle())
                .subtitle(req.getSubtitle())
                .content(req.getContent())
                .coverImageUrl(imageUrl)
                .category(category)
                .createdBy(staff)
                .sendNotification(req.getSendNotification())
                .publishAt(publishTime)
                .viewCount(0)
                .status(status)
                .targetAudience(Announcement.TargetAudience.ALL_RESIDENTS)
                .createdAt(now)
                .build();

        handlePinLogic(announcement, req.getPinned());

        Announcement saved = announcementRepository.save(announcement);

        if (status == Announcement.Status.PUBLISHED &&
                Boolean.TRUE.equals(saved.getSendNotification())) {

            sendAnnouncementNotification(saved);
        }

        return map(saved);
    }

    public AnnouncementDto updateAnnouncement(
            Integer id,
            UpdateAnnouncementDto req,
            MultipartFile image
    ) {
        Announcement ann = announcementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        Announcement.Status oldStatus = ann.getStatus();

        if (req.getTitle() != null)
            ann.setTitle(req.getTitle());
        if (req.getSubtitle() != null)
            ann.setSubtitle(req.getSubtitle());
        if (req.getContent() != null)
            ann.setContent(req.getContent());
        if (req.getCategoryId() != null) {
            AnnouncementCategory category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            ann.setCategory(category);
        }

        if (req.getSendNotification() != null) {
            ann.setSendNotification(req.getSendNotification());
        }

        LocalDateTime now = LocalDateTime.now();

        Announcement.Status newStatus;

        if (Boolean.TRUE.equals(req.getPublishNow())) {
            newStatus = Announcement.Status.PUBLISHED;
            ann.setStatus(newStatus);
            ann.setPublishAt(now);

        } else if (req.getPublishAt() != null && req.getPublishAt().isAfter(now)) {
            newStatus = Announcement.Status.DRAFT;
            ann.setStatus(newStatus);
            ann.setPublishAt(req.getPublishAt());

        } else {
            newStatus = ann.getStatus();
        }

        handlePinLogic(ann, req.getPinned());

        if (image != null && !image.isEmpty()) {
            if (ann.getCoverImageUrl() != null) {
                fileStorageService.deleteFileByUrl(ann.getCoverImageUrl());
            }
            String newImageUrl = fileStorageService.store(image);
            ann.setCoverImageUrl(newImageUrl);
        }

        ann.setUpdatedAt(now);

        Announcement saved = announcementRepository.save(ann);

        boolean justPublished = oldStatus != Announcement.Status.PUBLISHED
                && newStatus == Announcement.Status.PUBLISHED;

        if (justPublished && Boolean.TRUE.equals(saved.getSendNotification())) {
            sendAnnouncementNotification(saved);
        }

        return map(saved);
    }

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
                .sendNotification(a.getSendNotification())
                .build();
    }

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

    public List<AnnouncementDto> getAllForStaff() {

        return announcementRepository
                .findByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(this::map)
                .toList();
    }

    public AnnouncementDto getByIdForStaff(Integer id) {

        Announcement ann = announcementRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Announcement not found"));

        if (ann.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Announcement not found");
        }

        return map(ann);
    }
}