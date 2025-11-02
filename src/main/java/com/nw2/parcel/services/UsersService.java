package com.nw2.parcel.services;

import com.google.firebase.auth.FirebaseToken;
import com.nw2.parcel.Dtos.RegisterDto;
import com.nw2.parcel.entity.Dorm;
import com.nw2.parcel.entity.Users;
import com.nw2.parcel.repositories.DormRepository;
import com.nw2.parcel.repositories.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Service
public class UsersService {

    private final UsersRepository usersRepository;
    private final DormRepository dormRepository;

    public UsersService(UsersRepository usersRepository, DormRepository dormRepository) {
        this.usersRepository = usersRepository;
        this.dormRepository = dormRepository;
    }

    /** สมัคร (แนวทาง A): เก็บโปรไฟล์+role/dorm ก่อน */
    @Transactional
    public Users register(RegisterDto req) {
        final String email = normalize(req.email());
        if (email == null) throw new ResponseStatusException(BAD_REQUEST, "Email is required");

        if (usersRepository.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "Email already registered");
        }

        // role
        final Users.Role role = parseRole(req.role());

        Dorm dorm = null;

        if (role == Users.Role.RESIDENT) {
            // ✅ ลองหา dorm จาก id ก่อน
            if (req.dormId() != null && req.dormId() > 0) {
                dorm = dormRepository.findById(req.dormId())
                        .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Dorm not found"));
            }
            // ✅ ถ้าไม่มี dormId แต่มีชื่อหอ → หาใน DB
            else if (req.dormName() != null && !req.dormName().trim().isEmpty()) {
                dorm = dormRepository.findByDormNameIgnoreCase(req.dormName().trim())
                        .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Dorm name not found"));
            }
            else {
                throw new ResponseStatusException(BAD_REQUEST, "Dorm ID or Name is required for RESIDENT");
            }

            // ตรวจห้อง
            if (!hasText(req.roomNumber())) {
                throw new ResponseStatusException(BAD_REQUEST, "roomNumber is required for RESIDENT");
            }
        }
        else if (role == Users.Role.STAFF) {
            // ต้องมี position (ไม่ต้องมี dorm/roomNumber)
            if (!hasText(req.position())) {
                throw new ResponseStatusException(BAD_REQUEST, "position is required for STAFF");
            }
        }

        Users user = new Users();
        user.setEmail(email);
        user.setFirstName(safeTrim(req.firstName()));
        user.setLastName(safeTrim(req.lastName()));
        user.setRole(role);
        user.setStatus(Users.Status.ACTIVE);
        if (dorm != null) user.setDorm(dorm);
        user.setRoomNumber(safeTrim(req.roomNumber()));
        user.setPosition(safeTrim(req.position()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return usersRepository.save(user);
    }

    @Transactional
    public Users linkFirebaseOnLogin(FirebaseToken tok) {
        final String email = normalize(tok.getEmail());
        if (email == null) throw new ResponseStatusException(BAD_REQUEST, "Firebase account has no email");

        Users user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Please register before login"));

        if (user.getFirebaseUid() == null) {
            user.setFirebaseUid(tok.getUid());
        } else if (!user.getFirebaseUid().equals(tok.getUid())) {
            // กันเคส email นี้ถูกผูกกับ Firebase UID อื่นไปแล้ว
            throw new ResponseStatusException(CONFLICT, "Email already linked to another Firebase account");
        }

        // อัปเดตโปรไฟล์เบาๆ จาก Firebase (ตามเหมาะสม)
//        if (hasText(tok.getPicture())) user.setProfileImageUrl(tok.getPicture());
        // ถ้าต้องการแยกชื่อจาก tok.getName() ค่อยเพิ่ม split ที่นี่

        user.setUpdatedAt(LocalDateTime.now());
        return usersRepository.save(user);
    }

    // ---------- helpers ----------
    private String normalize(String s) { return s == null ? null : s.trim().toLowerCase(); }
    private String safeTrim(String s) { return s == null ? null : s.trim(); }
    private boolean hasText(String s) { return s != null && !s.trim().isEmpty(); }

    private Users.Role parseRole(String value) {
        if (!hasText(value)) throw new ResponseStatusException(BAD_REQUEST, "Role is required");
        try {
            Users.Role role = Users.Role.valueOf(value.trim().toUpperCase());
            if (role == Users.Role.ADMIN) {
                throw new ResponseStatusException(FORBIDDEN, "Cannot self-register as ADMIN");
            }
            return role;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(BAD_REQUEST, "Role must be RESIDENT or STAFF");
        }
    }
}

