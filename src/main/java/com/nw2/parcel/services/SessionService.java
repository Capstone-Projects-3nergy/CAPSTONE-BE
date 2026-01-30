package com.nw2.parcel.services;

import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final UsersRepository usersRepository;

    // รันทุก 10 นาที (600,000 มิลลิวินาที)
    @Transactional
    @Scheduled(fixedRate = 600000)
    public void autoLogoutInactiveUsers() {
        // ใครที่ไม่ Update นานเกิน 1 ชั่วโมง จะถูกเซตเป็น INACTIVE
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(1);

        List<Users> inactiveUsers = usersRepository.findByStatusAndUpdatedAtBefore(
                Users.Status.ACTIVE,
                threshold
        );

        if (!inactiveUsers.isEmpty()) {
            inactiveUsers.forEach(user -> {
                user.setStatus(Users.Status.INACTIVE);
                user.setUpdatedAt(LocalDateTime.now());
            });
            usersRepository.saveAll(inactiveUsers);
            System.out.println("Auto-logout: " + inactiveUsers.size() + " users processed.");
        }
    }
}