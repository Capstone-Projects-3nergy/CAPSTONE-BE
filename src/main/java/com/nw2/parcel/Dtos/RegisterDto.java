package com.nw2.parcel.Dtos;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record RegisterDto(

        // ข้อมูลพื้นฐาน
        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        // การกำหนดสิทธิ์/สังกัด
        @NotBlank(message = "Role is required")   // "RESIDENT" หรือ "STAFF"
        String role,

        // ✅ ใหม่ — frontend สามารถส่งได้ทั้ง dormId หรือ dormName
        Long dormId,
        String dormName,

        // ช่องเพิ่มตาม role
        String roomNumber,   // จำเป็นเมื่อ role=RESIDENT
        String position      // จำเป็นเมื่อ role=STAFF
) {
    // ===== Validation เฉพาะ role =====

    // Resident ต้องมี dormId หรือ dormName อย่างใดอย่างหนึ่ง
    @AssertTrue(message = "Dorm ID or Name is required for RESIDENT")
    @JsonIgnore
    public boolean isDormValidForResident() {
        return !isRole("RESIDENT") ||
                ((dormId != null && dormId > 0) || hasText(dormName));
    }

    @AssertTrue(message = "roomNumber is required for RESIDENT")
    @JsonIgnore
    public boolean isRoomNumberValidForResident() {
        return !isRole("RESIDENT") || hasText(roomNumber);
    }

    @AssertTrue(message = "position is required for STAFF")
    @JsonIgnore
    public boolean isPositionValidForStaff() {
        return !isRole("STAFF") || hasText(position);
    }

    // ===== Helper methods =====
    private boolean isRole(String target) {
        return role != null && role.trim().equalsIgnoreCase(target);
    }
    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
