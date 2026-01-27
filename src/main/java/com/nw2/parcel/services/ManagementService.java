package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.ManagementDetailDto;
import com.nw2.parcel.Dtos.ManagementListDto;
import com.nw2.parcel.Dtos.ManagementAddDto;
import com.nw2.parcel.entity.Trash;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.exception.EmailAlreadyExistsException;
import com.nw2.parcel.repositories.DormRepository;
import com.nw2.parcel.repositories.TrashRepository;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

//management
@Service
@RequiredArgsConstructor
public class ManagementService {

    private final UsersRepository usersRepository;
    private final DormRepository dormRepository;
    private final TrashRepository trashRepository;

    // 1️⃣ list resident
    public List<ManagementListDto> getAllResidents() {
        return usersRepository
                .findByRoleAndStatusNot(Users.Role.RESIDENT, Users.Status.DELETED)
                .stream()
                .map(user -> new ManagementListDto(
                        user.getUserId(),
                        user.getFirstName() + " " + user.getLastName(),
                        user.getEmail(),
                        user.getRoomNumber(),
                        user.getProfileImageUrl(),
                        user.getRole().name(),
                        user.getDorm() != null ? user.getDorm().getDormName() : null,
                        user.getStatus().name(),
                        user.getUpdatedAt()
                ))
                .toList();
    }

    // 2️⃣ detail
    public ManagementDetailDto getResidentDetail(Integer id) {
        Users user = usersRepository.findByUserIdAndRole(id, Users.Role.RESIDENT)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));

        ManagementDetailDto dto = new ManagementDetailDto();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRoomNumber(user.getRoomNumber());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setLineId(user.getLineId());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setRole(user.getRole().name());

        if (user.getDorm() != null) {
            dto.setDormId(user.getDorm().getDormId());
            dto.setDormName(user.getDorm().getDormName());
        }

        return dto;
    }


    // 3️⃣ add resident
    public void addResident(ManagementAddDto req) {
        if (usersRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        Users user = new Users();
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail());
        user.setRoomNumber(req.getRoomNumber());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setLineId(req.getLineId());
        user.setRole(Users.Role.RESIDENT);
        user.setStatus(Users.Status.ACTIVE);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (req.getDormId() != null) {
            user.setDorm(dormRepository.findById(req.getDormId())
                    .orElseThrow(() -> new IllegalArgumentException("Dorm not found")));
        }

        usersRepository.save(user);
    }

    // 4️⃣ update resident
    public void updateResident(Integer id, ManagementAddDto req) {
        Users user = usersRepository.findByUserIdAndRole(id, Users.Role.RESIDENT)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));

        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRoomNumber(req.getRoomNumber());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setLineId(req.getLineId());
        user.setUpdatedAt(LocalDateTime.now());

        usersRepository.save(user);
    }

    @Transactional
    public void softDeleteResident(Integer residentId, String staffEmail) {

        Users staff = usersRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new IllegalStateException("Staff not found"));

        Users resident = usersRepository
                .findByUserIdAndRole(residentId, Users.Role.RESIDENT)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));

        // ป้องกันลบซ้ำ
        if (resident.getStatus() == Users.Status.DELETED) {
            throw new IllegalStateException("Resident already deleted");
        }

        // soft delete
        resident.setStatus(Users.Status.DELETED);
        resident.setDeletedAt(LocalDateTime.now());
        resident.setUpdatedAt(LocalDateTime.now());

        usersRepository.save(resident);

        // move to trash
        Trash trash = new Trash();
        trash.setTargetType(Trash.TargetType.USER);
        trash.setTargetId(residentId);
        trash.setDeletedAt(LocalDateTime.now());
        trash.setDeletedBy(staff);

        trashRepository.save(trash);
    }

}

