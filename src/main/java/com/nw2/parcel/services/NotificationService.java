package com.nw2.parcel.services;

import com.nw2.parcel.entity.Notification;
import com.nw2.parcel.entity.Parcels;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void notifyResidentParcelMatched(Parcels parcel, Users resident) {

        Notification noti = new Notification();
        noti.setNotiTitle("New Parcel Arrived");
        noti.setNotiMessage(
                "Your parcel with tracking number "
                        + parcel.getTrackingNumber()
                        + " has arrived at the dormitory."
        );

        noti.setStatus(Notification.Status.PENDING);
        noti.setNotificationType(null); // หรือ null ก่อน
        noti.setParcel(parcel);
        noti.setUser(resident);

        noti.setCreatedAt(LocalDateTime.now());
        noti.setUpdatedAt(LocalDateTime.now());

        notificationRepository.save(noti);
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

}

