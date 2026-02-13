package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.NotificationDto;
import com.nw2.parcel.entity.Notification;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.exception.ResourceNotFoundException;
import com.nw2.parcel.exception.UnauthorizedException;
import com.nw2.parcel.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    // Parcel Notification
    public void notifyResidentParcelMatched(Parcels parcel, Users resident) {

        Notification noti = new Notification();
        noti.setNotiTitle("New Parcel Arrived");
        noti.setNotiMessage(
                "Your parcel with tracking number "
                        + parcel.getTrackingNumber()
                        + " has arrived at the dormitory."
        );

        noti.setStatus(Notification.Status.SENT);
        noti.setNotificationType(Notification.Type.SYSTEM);
        noti.setParcel(parcel);
        noti.setUser(resident);

        noti.setCreatedAt(LocalDateTime.now());
        noti.setUpdatedAt(LocalDateTime.now());
        noti.setSentAt(LocalDateTime.now());

        notificationRepository.save(noti);
    }

    //Generic System Notification
    public void createSystemNotification(
            Users user,
            String title,
            String message
    ) {
        Notification noti = new Notification();
        noti.setNotiTitle(title);
        noti.setNotiMessage(message);

        noti.setStatus(Notification.Status.SENT);
        noti.setNotificationType(Notification.Type.SYSTEM);
        noti.setParcel(null);
        noti.setUser(user);

        noti.setCreatedAt(LocalDateTime.now());
        noti.setUpdatedAt(LocalDateTime.now());
        noti.setSentAt(LocalDateTime.now());

        notificationRepository.save(noti);
    }

    // Multi Channel Notification (SYSTEM + EMAIL)
    @Transactional
    public void createMultiChannelNotification(
            Users user,
            String title,
            String message
    ) {
        LocalDateTime now = LocalDateTime.now();

        // SYSTEM (show on web)
        Notification systemNoti = new Notification();
        systemNoti.setNotiTitle(title);
        systemNoti.setNotiMessage(message);
        systemNoti.setNotificationType(Notification.Type.SYSTEM);
        systemNoti.setStatus(Notification.Status.SENT);
        systemNoti.setUser(user);
        systemNoti.setCreatedAt(now);
        systemNoti.setUpdatedAt(now);
        systemNoti.setSentAt(now);

        notificationRepository.save(systemNoti);

        // EMAIL (send only)
        Notification emailNoti = new Notification();
        emailNoti.setNotiTitle(title);
        emailNoti.setNotiMessage(message);
        emailNoti.setNotificationType(Notification.Type.EMAIL);
        emailNoti.setStatus(Notification.Status.PENDING);
        emailNoti.setUser(user);
        emailNoti.setCreatedAt(now);
        emailNoti.setUpdatedAt(now);

        notificationRepository.save(emailNoti);

        sendEmailAndUpdateStatus(emailNoti, user.getEmail());
    }

    //Get User Notifications
    public List<NotificationDto> getNotificationsByUser(Integer userId) {

        return notificationRepository
                .findByUserUserIdAndNotificationTypeOrderByCreatedAtDesc(
                        userId,
                        Notification.Type.SYSTEM
                )
                .stream()
                .map(n -> new NotificationDto(
                        n.getNotificationId(),
                        n.getNotiTitle(),
                        n.getNotiMessage(),
                        n.getStatus(),
                        n.getNotificationType(),
                        n.getCreatedAt(),
                        n.getSentAt(),
                        n.getIsRead(),
                        n.getReadAt(),
                        n.getParcel() != null ? n.getParcel().getParcelId() : null,
                        n.getParcel() != null ? n.getParcel().getTrackingNumber() : null
                ))
                .toList();
    }

    // Mark As Read
    public void markAsRead(Integer notificationId, Integer userId) {

        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Notification not found: " + notificationId)
                );

        if (!noti.getUser().getUserId().equals(userId)) {
            throw new UnauthorizedException("You cannot modify this notification");
        }

        if (!Boolean.TRUE.equals(noti.getIsRead())) {
            noti.setIsRead(true);
            noti.setReadAt(LocalDateTime.now());
            notificationRepository.save(noti);
        }
    }

    @Transactional
    public void notifyParcelMultiChannel(Parcels parcel, Users resident) {

        LocalDateTime now = LocalDateTime.now();

        // SYSTEM (in-app)
        Notification systemNoti = new Notification();
        systemNoti.setNotiTitle("New Parcel Arrived");
        systemNoti.setNotiMessage(
                "Your parcel with tracking number "
                        + parcel.getTrackingNumber()
                        + " has arrived."
        );
        systemNoti.setStatus(Notification.Status.SENT);
        systemNoti.setNotificationType(Notification.Type.SYSTEM);
        systemNoti.setParcel(parcel);
        systemNoti.setUser(resident);
        systemNoti.setCreatedAt(now);
        systemNoti.setUpdatedAt(now);
        systemNoti.setSentAt(now);

        notificationRepository.save(systemNoti);

        //EMAIL
        Notification emailNoti = new Notification();
        emailNoti.setNotiTitle("Parcel Arrival Notification");
        emailNoti.setNotiMessage(
                resident.getFirstName() +
                        ", your parcel (" + parcel.getTrackingNumber() +
                        ") has arrived at the dormitory."
        );
        emailNoti.setStatus(Notification.Status.PENDING);
        emailNoti.setNotificationType(Notification.Type.EMAIL);
        emailNoti.setParcel(parcel);
        emailNoti.setUser(resident);
        emailNoti.setCreatedAt(now);
        emailNoti.setUpdatedAt(now);

        notificationRepository.save(emailNoti);

        sendEmailAndUpdateStatus(emailNoti, resident.getEmail());
    }

    private void sendEmailAndUpdateStatus(Notification emailNoti, String email) {

        try {
            System.out.println("TRY SENDING EMAIL TO: " + email);

            emailService.send(
                    email,
                    emailNoti.getNotiTitle(),
                    emailNoti.getNotiMessage()
            );

            System.out.println("EMAIL SENT SUCCESS");

            emailNoti.setStatus(Notification.Status.SENT);
            emailNoti.setSentAt(LocalDateTime.now());

        } catch (Exception e) {

            System.out.println("EMAIL FAILED");
            e.printStackTrace();

            emailNoti.setStatus(Notification.Status.FAILED);

        }

        emailNoti.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(emailNoti);
    }

}
