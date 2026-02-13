package com.nw2.parcel.Dtos;

import com.nw2.parcel.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class NotificationDto {

    private Integer notificationId;
    private String title;
    private String message;
    private Notification.Status status;
    private Notification.Type type;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private Boolean isRead;
    private LocalDateTime readAt;
    private Integer parcelId;
    private String trackingNumber;
}
