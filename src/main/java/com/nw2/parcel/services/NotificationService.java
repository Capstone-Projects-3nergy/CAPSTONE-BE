package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.NotificationDto;
import com.nw2.parcel.entity.Notification;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Users;
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

    // ===============================
    // Parcel Notification
    // ===============================
    public void notifyResidentParcelMatched(Parcels parcel, Users resident) {

        Notification noti = new Notification();
        noti.setNotiTitle("New Parcel Arrived");
        noti.setNotiMessage(
                "Your parcel with tracking number "
                        + parcel.getTrackingNumber()
                        + " has arrived at the dormitory."
        );

        noti.setStatus(Notification.Status.SENT); // ✅ เปลี่ยนจาก PENDING
        noti.setNotificationType(Notification.Type.SYSTEM); // ✅ ไม่ null
        noti.setParcel(parcel);
        noti.setUser(resident);

        noti.setCreatedAt(LocalDateTime.now());
        noti.setUpdatedAt(LocalDateTime.now());
        noti.setSentAt(LocalDateTime.now());

        notificationRepository.save(noti);
    }

    // ===============================
    // 🔔 Generic System Notification
    // ===============================
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
        noti.setParcel(null); // สำคัญ
        noti.setUser(user);

        noti.setCreatedAt(LocalDateTime.now());
        noti.setUpdatedAt(LocalDateTime.now());
        noti.setSentAt(LocalDateTime.now());

        notificationRepository.save(noti);
    }

    // ===============================
    // 📥 Get User Notifications
    // ===============================
    public List<NotificationDto> getNotificationsByUser(Integer userId) {

        return notificationRepository
                .findByUserUserIdOrderByCreatedAtDesc(userId)
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

    // ===============================
    // Mark As Read
    // ===============================
    public void markAsRead(Integer notificationId, Integer userId) {

        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Notification not found: " + notificationId)
                );

        if (!noti.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("You cannot modify this notification");
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

        // ===============================
        // SYSTEM (in-app)
        // ===============================
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

        // ===============================
        // 📧 EMAIL
        // ===============================
        Notification emailNoti = new Notification();
        emailNoti.setNotiTitle("Parcel Arrival Notification");
        emailNoti.setNotiMessage(
                "Dear " + resident.getFirstName() +
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

        // 🔥 ส่ง email จริง
        sendEmailAndUpdateStatus(emailNoti, resident.getEmail());
    }

    private void sendEmailAndUpdateStatus(Notification emailNoti, String email) {

        try {
            emailService.send(
                    email,
                    emailNoti.getNotiTitle(),
                    emailNoti.getNotiMessage()
            );

            emailNoti.setStatus(Notification.Status.SENT);
            emailNoti.setSentAt(LocalDateTime.now());

        } catch (Exception e) {

            emailNoti.setStatus(Notification.Status.FAILED);
        }

        emailNoti.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(emailNoti);
    }

}


//    public void createIfNotExists(
//            String eventKey,
//            Users user,
//            Parcels parcel,
//            String title,
//            String message
//    ) {
//        if (notificationRepository.existsByEventKey(eventKey)) {
//            return;
//        }
//
//        Notification noti = new Notification();
////        noti.setEventKey(eventKey);
//        noti.setNotiTitle(title);
//        noti.setNotiMessage(message);
//        noti.setUser(user);
//        noti.setParcel(parcel);
//        noti.setStatus(Notification.Status.PENDING);
//
//        noti.setCreatedAt(LocalDateTime.now());
//        noti.setUpdatedAt(LocalDateTime.now());
//
//        notificationRepository.save(noti);
//    }