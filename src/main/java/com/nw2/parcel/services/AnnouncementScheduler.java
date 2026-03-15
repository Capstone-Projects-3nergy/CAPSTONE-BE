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

    // run every minute
    @Scheduled(fixedRate = 60000)
    public void publishScheduledAnnouncements() {

        List<Announcement> announcements =
                announcementRepository.findByStatusAndPublishAtBefore(
                        Announcement.Status.DRAFT,
                        LocalDateTime.now()
                );

        for (Announcement ann : announcements) {

            ann.setStatus(Announcement.Status.PUBLISHED);

            announcementRepository.save(ann);

            if (Boolean.TRUE.equals(ann.getSendNotification())) {

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
        }
    }
}