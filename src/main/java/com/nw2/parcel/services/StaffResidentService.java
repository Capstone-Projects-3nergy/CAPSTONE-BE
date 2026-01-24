package com.nw2.parcel.services;

import com.nw2.parcel.Dtos.ResidentDetailDto;
import com.nw2.parcel.Dtos.ResidentListResponse;
import com.nw2.parcel.Dtos.CreateResidentDto;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.exception.EmailAlreadyExistsException;
import com.nw2.parcel.repositories.DormRepository;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

//management
@Service
@RequiredArgsConstructor
public class StaffResidentService {

    private final UsersRepository usersRepository;
    private final DormRepository dormRepository;

    // 1️⃣ list resident
    public List<ResidentListResponse> getAllResidents() {
        return usersRepository.findByStatusNot(Users.Status.DELETED)
                        .stream()
                .map(user -> new ResidentListResponse(
                        user.getUserId(),
                        user.getFirstName() + " " + user.getLastName(),
                        user.getEmail(),
                        user.getRoomNumber(),
                        user.getProfileImageUrl(),
                        user.getStatus().name(),
                        user.getUpdatedAt()
                ))
                .toList();
    }

    // 2️⃣ detail
    public ResidentDetailDto getResidentDetail(Integer id) {
        Users user = usersRepository.findByUserIdAndRole(id, Users.Role.RESIDENT)
                .orElseThrow(() -> new IllegalArgumentException("Resident not found"));

        ResidentDetailDto dto = new ResidentDetailDto();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setRoomNumber(user.getRoomNumber());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setLineId(user.getLineId());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setDormId(user.getDorm() != null ? user.getDorm().getDormId() : null);

        return dto;
    }

    // 3️⃣ add resident
    public void addResident(CreateResidentDto req) {
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
    public void updateResident(Integer id, CreateResidentDto req) {
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


}

