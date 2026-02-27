package com.xparience.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(

        String email,

        String phoneNumber,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @NotBlank(message = "Please confirm your password")
        String confirmPassword,

        @NotNull(message = "Date of birth is required")
        LocalDate dateOfBirth,

        boolean termsAccepted,

        boolean privacyPolicyAccepted
) {}