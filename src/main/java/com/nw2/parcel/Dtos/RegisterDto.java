package com.nw2.parcel.Dtos;

import jakarta.validation.constraints.*;
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

        @NotNull(message = "Dorm ID is required")
        @Positive(message = "Dorm ID must be positive")
        Long dormId,

        // ช่องเพิ่มตาม role
        String roomNumber,   // จำเป็นเมื่อ role=RESIDENT
        String position      // จำเป็นเมื่อ role=STAFF
) {
    // ----- Cross-field validations (ตาม role) -----

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

    // ----- helpers -----
    private boolean isRole(String target) {
        return role != null && role.trim().equalsIgnoreCase(target);
    }
    private boolean hasText(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
