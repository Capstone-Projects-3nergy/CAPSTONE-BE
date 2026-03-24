package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
//    boolean existsByEventKey(String eventKey);
    List<Notification> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
    List<Notification> findByUserUserIdAndNotificationTypeOrderByCreatedAtDesc(Integer userId, Notification.Type type);
    boolean existsByUserUserIdAndParcelParcelIdAndNotificationType(
            Integer userId,
            Integer parcelId,
            Notification.Type type
    );
    Optional<Notification> findTopByUserUserIdAndParcelParcelIdAndNotificationTypeOrderByCreatedAtDesc(
            Integer userId,
            Integer parcelId,
            Notification.Type type
    );
    List<Notification> findByUserUserIdAndParcelParcelIdAndNotificationTypeOrderByCreatedAtDesc(
            Integer userId,
            Integer parcelId,
            Notification.Type type
    );
    List<Notification> findByUserUserIdAndParcelParcelIdAndNotificationTypeInOrderByCreatedAtDesc(
            Integer userId,
            Integer parcelId,
            List<Notification.Type> types
    );
    List<Notification> findByUserUserIdAndNotificationTypeInOrderByCreatedAtDesc(
            Integer userId,
            List<Notification.Type> types
    );

    boolean existsByParcelParcelIdAndNotificationType(Integer parcelId, Notification.Type type);
}
