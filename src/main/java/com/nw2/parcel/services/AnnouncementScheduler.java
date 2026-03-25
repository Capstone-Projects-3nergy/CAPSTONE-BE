package com.nw2.parcel.services;

import com.nw2.parcel.entity.Announcement;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.AnnouncementRepository;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementScheduler {

    private final AnnouncementRepository announcementRepository;
    private final UsersRepository usersRepository;
    private final NotificationService notificationService;

    @Scheduled(fixedRate = 60000)
    public void publishScheduledAnnouncements() {

        LocalDateTime now = LocalDateTime.now();

        List<Announcement> announcements =
                announcementRepository.findByStatusAndDeletedAtIsNullAndPublishAtBefore(
                        Announcement.Status.DRAFT,
                        now
                );

        for (Announcement ann : announcements) {

            try {
                // 🔥 กันยิงซ้ำระดับ DB (สำคัญ)
                if (ann.getStatus() != Announcement.Status.DRAFT) continue;

                ann.setStatus(Announcement.Status.PUBLISHED);
                Announcement saved = announcementRepository.save(ann);

                if (Boolean.TRUE.equals(saved.getSendNotification())) {

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

            } catch (Exception e) {
                // ❗ log error ไว้ debug
                System.err.println("Failed to publish announcement ID: " + ann.getAnnouncementId());
                e.printStackTrace();
            }
        }
    }
}