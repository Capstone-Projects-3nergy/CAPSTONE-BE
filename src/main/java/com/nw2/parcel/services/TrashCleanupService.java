package com.nw2.parcel.services;

import com.nw2.parcel.entity.Trash;
import com.nw2.parcel.repositories.ParcelsRepository;
import com.nw2.parcel.repositories.TrashRepository;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrashCleanupService {

    private final TrashRepository trashRepository;
    private final ParcelsRepository parcelsRepository;
    private final UsersRepository usersRepository;

    @Value("${trash.retention-days}")
    private int retentionDays;

    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredTrash() {

        LocalDateTime expiredAt =
                LocalDateTime.now().minusDays(retentionDays);

        List<Trash> expiredTrash =
                trashRepository.findAllByDeletedAtBefore(expiredAt);

        for (Trash trash : expiredTrash) {

            if (trash.getTargetType() == Trash.TargetType.PARCEL) {
                parcelsRepository.deleteById(trash.getTargetId());
            }

            if (trash.getTargetType() == Trash.TargetType.USER) {
                usersRepository.deleteById(trash.getTargetId());
            }

            trashRepository.delete(trash);
        }
    }
}
