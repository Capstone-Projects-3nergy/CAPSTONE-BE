package com.nw2.parcel.repositories;


import com.nw2.parcel.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {}
