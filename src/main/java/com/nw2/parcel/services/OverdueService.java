package com.nw2.parcel.services;

import com.nw2.parcel.entity.Notification;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.NotificationRepository;
import com.nw2.parcel.repositories.ParcelsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OverdueService {

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final ParcelsRepository parcelsRepository;

    public void remindByParcelId(Integer parcelId) {

        Parcels parcel = parcelsRepository.findById(parcelId)
                .orElseThrow(() -> new RuntimeException("Parcel not found"));

        sendOverdueReminder(parcel);
    }

    public void sendOverdueReminder(Parcels parcel) {

        Users user = parcel.getUser();

        // ไม่มี user → ไม่ต้องส่ง
        if (user == null) return;

        // parcel ไม่ใช่สถานะ RECEIVED → ไม่ต้องส่ง
        if (parcel.getStatus() != Parcels.Status.RECEIVED) return;

        // receivedAt เป็น null → กันพัง
        if (parcel.getReceivedAt() == null) return;

        LocalDateTime now = LocalDateTime.now();

        // ยังไม่ครบ 3 วัน → ไม่ส่ง
        LocalDateTime overdueTime = parcel.getReceivedAt().plusDays(3);
        if (now.isBefore(overdueTime)) {
            return;
        }

        // หา history ทั้ง SYSTEM + LINE
        List<Notification> history =
                notificationRepository
                        .findByUserUserIdAndParcelParcelIdAndNotificationTypeInOrderByCreatedAtDesc(
                                user.getUserId(),
                                parcel.getParcelId(),
                                List.of(
                                        Notification.Type.OVERDUE_SYSTEM,
                                        Notification.Type.OVERDUE_LINE
                                )
                        );

        if (!history.isEmpty()) {
            Notification last = history.get(0);

            // ยังไม่ครบ 3 วันจากครั้งล่าสุด → ไม่ส่ง
            if (last.getCreatedAt() != null &&
                    last.getCreatedAt().plusDays(3).isAfter(now)) {
                return;
            }
        }

        // ส่ง notification
        notificationService.notifyParcelOverdue(parcel, user);
    }
}