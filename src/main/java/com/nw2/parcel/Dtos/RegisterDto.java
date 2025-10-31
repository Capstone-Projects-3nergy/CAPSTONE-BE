package com.nw2.parcel.Dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public record RegisterDto(

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Phone number is required")
        @Size(max = 15, message = "Phone number too long")
        String phoneNumber,

        String profileImageUrl,

        @NotBlank(message = "Role is required")
        String role,  // RESIDENT หรือ STAFF

        @NotNull(message = "Dorm ID is required")
        Long dormId,  // id ของหอพัก (foreign key)

        String roomNumber, // สำหรับ resident เท่านั้น
        String lineId,     // Optional
        String position    // สำหรับ staff เท่านั้น
) {}
