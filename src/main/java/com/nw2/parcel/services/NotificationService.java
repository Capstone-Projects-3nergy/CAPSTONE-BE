package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.NotificationDto;
import com.nw2.parcel.entity.Notification;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.exception.ResourceNotFoundException;
import com.nw2.parcel.exception.UnauthorizedException;
import com.nw2.parcel.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;
    private final LineService lineService;
    private final SimpMessagingTemplate messagingTemplate;

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

        Notification saved = notificationRepository.save(noti);

        NotificationDto dto = new NotificationDto(
                saved.getNotificationId(),
                saved.getNotiTitle(),
                saved.getNotiMessage(),
                saved.getStatus(),
                saved.getNotificationType(),
                saved.getCreatedAt(),
                saved.getSentAt(),
                saved.getIsRead(),
                saved.getReadAt(),
                saved.getParcel() != null ? saved.getParcel().getParcelId() : null,
                saved.getParcel() != null ? saved.getParcel().getTrackingNumber() : null
        );

        messagingTemplate.convertAndSendToUser(
                resident.getFirebaseUid(),
                "/queue/notifications",
                dto
        );
    }

    // Generic System Notification
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

    // Get User Notifications
    public List<NotificationDto> getNotificationsByUser(Integer userId) {

        return notificationRepository
                .findByUserUserIdAndNotificationTypeInOrderByCreatedAtDesc(
                        userId,
                        List.of(
                                Notification.Type.SYSTEM,
                                Notification.Type.OVERDUE_SYSTEM
                        )
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

        // ---------------- SYSTEM ----------------
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

        Notification saved = notificationRepository.save(systemNoti);

        NotificationDto dto = new NotificationDto(
                saved.getNotificationId(),
                saved.getNotiTitle(),
                saved.getNotiMessage(),
                saved.getStatus(),
                saved.getNotificationType(),
                saved.getCreatedAt(),
                saved.getSentAt(),
                saved.getIsRead(),
                saved.getReadAt(),
                saved.getParcel() != null ? saved.getParcel().getParcelId() : null,
                saved.getParcel() != null ? saved.getParcel().getTrackingNumber() : null
        );

        messagingTemplate.convertAndSendToUser(
                resident.getFirebaseUid(),
                "/queue/notifications",
                dto
        );

        // ---------------- EMAIL ----------------
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

        // ---------------- LINE ----------------
        Notification lineNoti = new Notification();
        lineNoti.setNotiTitle("New Parcel Arrived");
        lineNoti.setNotiMessage("Parcel arrived.");
        lineNoti.setStatus(Notification.Status.PENDING);
        lineNoti.setNotificationType(Notification.Type.LINE);
        lineNoti.setParcel(parcel);
        lineNoti.setUser(resident);
        lineNoti.setCreatedAt(now);
        lineNoti.setUpdatedAt(now);

        notificationRepository.save(lineNoti);

        String statusText = switch (parcel.getStatus()) {
            case RECEIVED -> "Ready for Pickup";
            case PICKED_UP -> "Picked Up";
            default -> "Processing";
        };

        String viewUrl =
                "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2";

        var bubble = lineService.buildParcelFlex(
                parcel.getTrackingNumber(),
                statusText,
                viewUrl
        );

        var flexMessage = lineService.buildFlexMessage(bubble);

        sendLineAndUpdateStatus(lineNoti, resident, flexMessage);
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

    private void sendLineAndUpdateStatus(Notification lineNoti, Users user, Object messageBody) {

        try {
            if (user.getLineUserId() == null) {
                lineNoti.setStatus(Notification.Status.FAILED);
            } else {

                lineService.pushMessage(user.getLineUserId(), messageBody);

                lineNoti.setStatus(Notification.Status.SENT);
                lineNoti.setSentAt(LocalDateTime.now());
            }

        } catch (Exception e) {
            lineNoti.setStatus(Notification.Status.FAILED);
        }

        lineNoti.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(lineNoti);
    }

    public void notifyAnnouncement(Users user, String title, String message) {

        createSystemNotification(user, title, message);

        if (user.getLineUserId() != null) {

            Notification lineNoti = new Notification();
            lineNoti.setNotiTitle(title);
            lineNoti.setNotiMessage(message);
            lineNoti.setStatus(Notification.Status.PENDING);
            lineNoti.setNotificationType(Notification.Type.LINE);
            lineNoti.setUser(user);

            notificationRepository.save(lineNoti);

            var bubble = lineService.buildAnnouncementFlex(title, message);
            var flex = lineService.buildFlexMessage(bubble);

            sendLineAndUpdateStatus(lineNoti, user, flex);
        }
    }

    public void notifyParcelOverdue(Parcels parcel, Users user) {

        String message = "⏰ Parcel " + parcel.getTrackingNumber()
                + " is overdue for pickup (more than 3 days).";

        // ---------------- SYSTEM ----------------
        Notification systemNoti = new Notification();
        systemNoti.setNotiTitle("Parcel Overdue");
        systemNoti.setNotiMessage(message);
        systemNoti.setStatus(Notification.Status.SENT);
        systemNoti.setNotificationType(Notification.Type.OVERDUE_SYSTEM);
        systemNoti.setParcel(parcel);
        systemNoti.setUser(user);

        notificationRepository.save(systemNoti);

//        // ---------------- LINE ----------------
//        if (user.getLineUserId() != null) {
//
//            Notification lineNoti = new Notification();
//            lineNoti.setNotiTitle("Parcel Overdue");
//            lineNoti.setNotiMessage(message);
//            lineNoti.setStatus(Notification.Status.SENT);
//            lineNoti.setNotificationType(Notification.Type.OVERDUE_LINE);
//            lineNoti.setParcel(parcel);
//            lineNoti.setUser(user);
//
//            notificationRepository.save(lineNoti);
//
//            long days = Math.max(0,
//                    Duration.between(parcel.getReceivedAt(), LocalDateTime.now()).toDays()
//            );
//
//            // ยังไม่ถึง 3 วัน → ไม่ต้องส่ง
//            if (days < 3) return;
//
//            // กันยิงซ้ำ
//            boolean alreadySent = notificationRepository
//                    .existsByParcelParcelIdAndNotificationType(
//                            parcel.getParcelId(),
//                            Notification.Type.OVERDUE_LINE
//                    );
//
//            if (alreadySent) return;
//
//            String viewUrl =
//                    "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2/parcel/";
//
//            var flex = lineService.buildOverdueFlex(
//                    parcel.getTrackingNumber(),
//                    String.valueOf(days),
//                    viewUrl
//            );
//
//            var msg = lineService.buildFlexMessage(flex);
//
//            sendLineAndUpdateStatus(lineNoti, user, msg);
        // ---------------- LINE ----------------
        if (user.getLineUserId() == null) return; // ✅ ไม่มี LINE → จบแค่นี้

        // ✅ FIX Bug 2: เช็ค duplicate ก่อน save เสมอ
        boolean alreadySent = notificationRepository
                .existsByParcelParcelIdAndNotificationType(
                        parcel.getParcelId(),
                        Notification.Type.OVERDUE_LINE
                );

        if (alreadySent) return;

        // ✅ FIX Bug 1: ลบ days < 3 ออก เพราะ OverdueService เช็คแล้ว
        long days = Math.max(0,
                Duration.between(parcel.getReceivedAt(), LocalDateTime.now()).toDays()
        );

        Notification lineNoti = new Notification();
        lineNoti.setNotiTitle("Parcel Overdue");
        lineNoti.setNotiMessage(message);
        lineNoti.setStatus(Notification.Status.PENDING);
        lineNoti.setNotificationType(Notification.Type.OVERDUE_LINE);
        lineNoti.setParcel(parcel);
        lineNoti.setUser(user);
        lineNoti.setCreatedAt(LocalDateTime.now());
        lineNoti.setUpdatedAt(LocalDateTime.now());

        notificationRepository.save(lineNoti);

        String viewUrl = "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2/parcel/";

        var flex = lineService.buildOverdueFlex(
                parcel.getTrackingNumber(),
                String.valueOf(days),
                viewUrl
        );

        var msg = lineService.buildFlexMessage(flex);

        sendLineAndUpdateStatus(lineNoti, user, msg);
        }

    //dev day < 3
    //public void notifyParcelOverdue(Parcels parcel, Users user) {
    //
    //    String message = "⏰ Parcel " + parcel.getTrackingNumber()
    //            + " is overdue for pickup (more than 3 days).";
    //
    //    // ---------------- SYSTEM ----------------
    //    Notification systemNoti = new Notification();
    //    systemNoti.setNotiTitle("Parcel Overdue");
    //    systemNoti.setNotiMessage(message);
    //    systemNoti.setStatus(Notification.Status.SENT);
    //    systemNoti.setNotificationType(Notification.Type.OVERDUE_SYSTEM);
    //    systemNoti.setParcel(parcel);
    //    systemNoti.setUser(user);
    //    systemNoti.setCreatedAt(LocalDateTime.now());
    //    systemNoti.setUpdatedAt(LocalDateTime.now());
    //    systemNoti.setSentAt(LocalDateTime.now());
    //
    //    notificationRepository.save(systemNoti);
    //
    //    // ---------------- LINE ----------------
    //    if (user.getLineUserId() == null) return; // ✅ ไม่มี LINE → จบแค่นี้
    //
    //    // ✅ FIX Bug 2: เช็ค duplicate ก่อน save เสมอ
    //    boolean alreadySent = notificationRepository
    //            .existsByParcelParcelIdAndNotificationType(
    //                    parcel.getParcelId(),
    //                    Notification.Type.OVERDUE_LINE
    //            );
    //
    //    if (alreadySent) return;
    //
    //    // ✅ FIX Bug 1: ลบ days < 3 ออก เพราะ OverdueService เช็คแล้ว
    //    long days = Math.max(0,
    //            Duration.between(parcel.getReceivedAt(), LocalDateTime.now()).toDays()
    //    );
    //
    //    Notification lineNoti = new Notification();
    //    lineNoti.setNotiTitle("Parcel Overdue");
    //    lineNoti.setNotiMessage(message);
    //    lineNoti.setStatus(Notification.Status.PENDING);
    //    lineNoti.setNotificationType(Notification.Type.OVERDUE_LINE);
    //    lineNoti.setParcel(parcel);
    //    lineNoti.setUser(user);
    //    lineNoti.setCreatedAt(LocalDateTime.now());
    //    lineNoti.setUpdatedAt(LocalDateTime.now());
    //
    //    notificationRepository.save(lineNoti);
    //
    //    String viewUrl = "https://bscit.sit.kmutt.ac.th/capstone25/cp25nw2/parcel/";
    //
    //    var flex = lineService.buildOverdueFlex(
    //            parcel.getTrackingNumber(),
    //            String.valueOf(days),
    //            viewUrl
    //    );
    //
    //    var msg = lineService.buildFlexMessage(flex);
    //
    //    sendLineAndUpdateStatus(lineNoti, user, msg);
    //}
}