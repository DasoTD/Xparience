package com.xparience.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyTwoFactorRequest(
        @NotBlank(message = "Identifier is required")
        String identifier,

        @NotBlank(message = "OTP is required")
        String otp
) {}
