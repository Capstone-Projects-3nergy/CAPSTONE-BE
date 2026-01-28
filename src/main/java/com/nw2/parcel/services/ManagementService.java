package com.nw2.parcel.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.nw2.parcel.Dtos.ManagementDetailDto;
import com.nw2.parcel.Dtos.ManagementListDto;
import com.nw2.parcel.Dtos.ManagementAddDto;
import com.nw2.parcel.entity.Trash;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.DormRepository;
import com.nw2.parcel.repositories.TrashRepository;
import com.nw2.parcel.repositories.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagementService {

    private final UsersRepository usersRepository;
    private final DormRepository dormRepository;
    private final TrashRepository trashRepository;
    private final FileStorageService fileStorageService;

    //list resident
    public List<ManagementListDto> getAllResidents() {
        return usersRepository
                .findByRoleInAndStatusNot(
                        List.of(Users.Role.RESIDENT, Users.Role.STAFF),
                        Users.Status.DELETED
                )
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

    //detail
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

    //add resident ✅ แก้ไขแล้ว
    @Transactional
    public Users addResident(ManagementAddDto req, MultipartFile profileImage) {

        if (usersRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        //สร้าง Firebase user
        UserRecord firebaseUser;
        try {
            UserRecord.CreateRequest fbReq = new UserRecord.CreateRequest()
                    .setEmail(req.getEmail())
                    .setEmailVerified(false)
                    .setDisabled(false);

            firebaseUser = FirebaseAuth.getInstance().createUser(fbReq);

            //ส่งอีเมลให้ตั้งรหัสผ่านเอง
            FirebaseAuth.getInstance()
                    .generatePasswordResetLink(req.getEmail());

        } catch (FirebaseAuthException e) {
            throw new IllegalStateException("Cannot create Firebase user", e);
        }

        //สร้าง user ใน DB
        Users user = new Users();
        user.setFirebaseUid(firebaseUser.getUid());
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setEmail(req.getEmail());
        user.setRoomNumber(req.getRoomNumber());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setLineId(req.getLineId());
        user.setRole(Users.Role.RESIDENT);
        user.setStatus(Users.Status.PENDING);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        if (req.getDormId() != null) {
            user.setDorm(dormRepository.findById(req.getDormId())
                    .orElseThrow(() -> new IllegalArgumentException("Dorm not found")));
        }

        Users savedUser = usersRepository.save(user);

        // รูป (เหมือนเดิม)
        if (profileImage != null && !profileImage.isEmpty()) {
            String imageUrl = fileStorageService.uploadProfileImage(
                    profileImage,
                    savedUser.getUserId()
            );
            savedUser.setProfileImageUrl(imageUrl);
            savedUser = usersRepository.save(savedUser);
        }

        return savedUser;
    }


    //update resident องรับ multipart
    @Transactional
    public Users updateResident(Integer id, ManagementAddDto req, MultipartFile profileImage) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // อัปเดตข้อมูล
        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRoomNumber(req.getRoomNumber());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setLineId(req.getLineId());
        user.setUpdatedAt(LocalDateTime.now());

        // อัปเดต dorm ถ้ามี
        if (req.getDormId() != null) {
            user.setDorm(dormRepository.findById(req.getDormId())
                    .orElseThrow(() -> new IllegalArgumentException("Dorm not found")));
        }

        // Upload รูปใหม่ถ้ามี
        if (profileImage != null && !profileImage.isEmpty()) {
            // ลบรูปเก่าก่อน
            if (user.getProfileImageUrl() != null) {
                fileStorageService.deleteFileByUrl(user.getProfileImageUrl());
            }

            String newImageUrl = fileStorageService.uploadProfileImage(
                    profileImage,
                    user.getUserId()
            );
            user.setProfileImageUrl(newImageUrl);
        }

        return usersRepository.save(user);
    }

    //move to trash
    @Transactional
    public void softDeleteResident(Integer userId) {
        // 1) หา user ที่จะถูกลบ
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getStatus() == Users.Status.DELETED) {
            throw new IllegalStateException("User already deleted");
        }

        if (trashRepository
                .findByTargetTypeAndTargetId(Trash.TargetType.USER, userId)
                .isPresent()) {
            throw new IllegalStateException("User already in trash");
        }

        // 2) หา staff ที่ login อยู่
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String firebaseUid = auth.getName();

        Users staff = usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new IllegalStateException("Staff not found"));

        // 3) soft delete user
        user.setStatus(Users.Status.DELETED);
        user.setDeletedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);

        // 4) insert trash
        Trash trash = new Trash();
        trash.setTargetType(Trash.TargetType.USER);
        trash.setTargetId(userId);
        trash.setDeletedAt(LocalDateTime.now());
        trash.setDeletedBy(staff);
        trashRepository.save(trash);
    }
}