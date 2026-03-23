package com.nw2.parcel.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.nw2.parcel.Dtos.ManagementDetailDto;
import com.nw2.parcel.Dtos.ManagementListDto;
import com.nw2.parcel.Dtos.ManagementAddDto;
import com.nw2.parcel.Dtos.ManagementUpdateDto;
import com.nw2.parcel.entity.Trash;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.exception.ConflictException;
import com.nw2.parcel.exception.EmailAlreadyExistsException;
import com.nw2.parcel.exception.ExternalServiceException;
import com.nw2.parcel.exception.ResourceNotFoundException;
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
    private final EmailService emailService;
    private final FirebaseAuthService firebaseAuthService;

    public List<ManagementListDto> getAllResidents() {
        return usersRepository
                .findByRoleInAndStatusNot(
                        List.of(Users.Role.RESIDENT, Users.Role.STAFF),
                        Users.Status.DELETED
                )
                .stream()
                .map(user -> {
                    String dormName = null;
                    if (user.getDorm() != null) {
                        dormName = user.getDorm().getDormName();
                    }

                    return new ManagementListDto(
                            user.getUserId(),
                            user.getFirstName() + " " + user.getLastName(),
                            user.getEmail(),
                            user.getRoomNumber(),
                            user.getProfileImageUrl(),
                            user.getRole().name(),
                            dormName,
                            user.getStatus().name(),
                            user.getUpdatedAt()
                    );
                })
                .toList();
    }

    //detail
    public ManagementDetailDto getResidentDetail(Integer id) {
        Users user = usersRepository.findByUserIdAndRole(id, Users.Role.RESIDENT)
                .orElseThrow(() -> new ResourceNotFoundException("Resident not found"));

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
        dto.setStatus(user.getStatus().name());

        if (user.getDorm() != null) {
            dto.setDormId(user.getDorm().getDormId());
            dto.setDormName(user.getDorm().getDormName());
        }

        return dto;
    }

    //add
    @Transactional
    public ManagementDetailDto addResident(ManagementAddDto req, MultipartFile profileImage) {

        if (usersRepository.existsByEmail(req.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        //สร้าง Firebase user
        UserRecord firebaseUser;
        try {
            firebaseUser = firebaseAuthService.createUser(req.getEmail());

            String resetLink = FirebaseAuth.getInstance()
                    .generatePasswordResetLink(req.getEmail());

            String verifyLink = FirebaseAuth.getInstance()
                    .generateEmailVerificationLink(req.getEmail());

            emailService.send(
                    req.getEmail(),
                    "Activate your account",
                    """
                    Your account has been created by staff.
        
                    1) Verify your email:
                    %s
        
                    2) Set your password:
                    %s
                    """.formatted(verifyLink, resetLink)
            );

        } catch (FirebaseAuthException e) {
            throw new ExternalServiceException("Cannot create Firebase user", e);
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
                    .orElseThrow(() -> new ResourceNotFoundException("Dorm not found")));
        }

        Users savedUser = usersRepository.save(user);

        // รูป
        if (profileImage != null && !profileImage.isEmpty()) {
            String imageUrl = fileStorageService.uploadProfileImage(
                    profileImage,
                    savedUser.getUserId()
            );
            savedUser.setProfileImageUrl(imageUrl);
            savedUser = usersRepository.save(savedUser);
        }


        ManagementDetailDto dto = new ManagementDetailDto();
        dto.setUserId(savedUser.getUserId());
        dto.setFirstName(savedUser.getFirstName());
        dto.setLastName(savedUser.getLastName());
        dto.setEmail(savedUser.getEmail());
        dto.setRoomNumber(savedUser.getRoomNumber());
        dto.setPhoneNumber(savedUser.getPhoneNumber());
        dto.setLineId(savedUser.getLineId());
        dto.setProfileImageUrl(savedUser.getProfileImageUrl());
        dto.setRole(savedUser.getRole().name());
        dto.setStatus(user.getStatus().name());

        if (savedUser.getDorm() != null) {
            dto.setDormId(savedUser.getDorm().getDormId());
            dto.setDormName(savedUser.getDorm().getDormName());
        }

        return dto;
    }

    //update
    @Transactional
    public ManagementDetailDto updateResident(Integer id, ManagementUpdateDto req, MultipartFile profileImage) {
        Users user = usersRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setFirstName(req.getFirstName());
        user.setLastName(req.getLastName());
        user.setRoomNumber(req.getRoomNumber());
        user.setPhoneNumber(req.getPhoneNumber());
        user.setLineId(req.getLineId());
        user.setUpdatedAt(LocalDateTime.now());

        if (req.getDormId() != null) {
            user.setDorm(dormRepository.findById(req.getDormId())
                    .orElseThrow(() -> new IllegalArgumentException("Dorm not found")));
        }

        if (profileImage != null && !profileImage.isEmpty()) {
            if (user.getProfileImageUrl() != null) {
                fileStorageService.deleteFileByUrl(user.getProfileImageUrl());
            }
            String newImageUrl = fileStorageService.uploadProfileImage(profileImage, user.getUserId());
            user.setProfileImageUrl(newImageUrl);
        }

        Users savedUser = usersRepository.save(user);

        ManagementDetailDto dto = new ManagementDetailDto();
        dto.setUserId(savedUser.getUserId());
        dto.setFirstName(savedUser.getFirstName());
        dto.setLastName(savedUser.getLastName());
        dto.setEmail(savedUser.getEmail());
        dto.setRoomNumber(savedUser.getRoomNumber());
        dto.setPhoneNumber(savedUser.getPhoneNumber());
        dto.setLineId(savedUser.getLineId());
        dto.setProfileImageUrl(savedUser.getProfileImageUrl());
        dto.setRole(savedUser.getRole().name());
        dto.setStatus(user.getStatus().name());

        if (savedUser.getDorm() != null) {
            dto.setDormId(savedUser.getDorm().getDormId());
            dto.setDormName(savedUser.getDorm().getDormName());
        }

        return dto;
    }

    //move to trash
    @Transactional
    public void softDeleteResident(Integer userId) {

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() == Users.Status.DELETED) {
            throw new ConflictException("User already deleted");
        }

        if (trashRepository
                .findByTargetTypeAndTargetId(Trash.TargetType.USER, userId)
                .isPresent()) {
            throw new ConflictException("User already in trash");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String firebaseUid = auth.getName();

        Users staff = usersRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));

        user.setStatus(Users.Status.DELETED);
        user.setDeletedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        usersRepository.save(user);

        Trash trash = new Trash();
        trash.setTargetType(Trash.TargetType.USER);
        trash.setTargetId(userId);
        trash.setDeletedAt(LocalDateTime.now());
        trash.setDeletedBy(staff);
        trashRepository.save(trash);
    }

    // ─── Resend Verification Email ───────────────────────────────────────────────

    public void resendVerificationEmail(Integer userId) {

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getStatus() != Users.Status.PENDING) {
            throw new ConflictException(
                    "Can only resend verification to PENDING users (current status: " + user.getStatus() + ")"
            );
        }

        try {
            String resetLink = FirebaseAuth.getInstance()
                    .generatePasswordResetLink(user.getEmail());

            String verifyLink = FirebaseAuth.getInstance()
                    .generateEmailVerificationLink(user.getEmail());

            emailService.send(
                    user.getEmail(),
                    "Reminder: Activate your account",
                    """
                    This is a reminder to activate your account.
        
                    1) Verify your email:
                    %s
        
                    2) Set your password:
                    %s
        
                    If you have already completed these steps, please ignore this email.
                    """.formatted(verifyLink, resetLink)
            );

        } catch (FirebaseAuthException e) {
            throw new ExternalServiceException("Failed to generate activation links", e);
        }
    }
}