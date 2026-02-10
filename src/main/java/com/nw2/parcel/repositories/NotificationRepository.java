package com.nw2.parcel.repositories;

import com.nw2.parcel.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    boolean existsByEventKey(String eventKey);
    List<Notification> findByUserUserIdOrderByCreatedAtDesc(Integer userId);
}
