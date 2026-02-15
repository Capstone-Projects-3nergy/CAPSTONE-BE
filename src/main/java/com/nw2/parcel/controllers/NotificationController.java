package com.nw2.parcel.controllers;

import com.nw2.parcel.Dtos.NotificationDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import com.nw2.parcel.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://cp25nw2.sit.kmutt.ac.th",
        "https://cp25nw2.sit.kmutt.ac.th",
        "http://cp25nw2.sit.kmutt.ac.th",
        "https://cp25nw2.sit.kmutt.ac.th"
})
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UsersRepository usersRepository;

    private Users getCurrentUser(Authentication authentication) {

        String firebaseUid = authentication.getName();

        return usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found")
                );
    }

    //ดึง notification ของ user ที่ login อยู่
    @GetMapping
    public List<NotificationDto> getMyNotifications(Authentication authentication) {

        String firebaseUid = authentication.getName();

        Users user = usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return notificationService.getNotificationsByUser(user.getUserId());
    }

    //mark as read
    @PatchMapping("/{id}/read")
    public void markAsRead(
            @PathVariable Integer id,
            Authentication authentication
    ) {
        Users user = getCurrentUser(authentication);
        notificationService.markAsRead(id, user.getUserId());
    }
}