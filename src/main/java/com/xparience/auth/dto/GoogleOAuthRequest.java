package com.xparience.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record GoogleOAuthRequest(
        @NotBlank(message = "Google account email is required")
        @Email(message = "Google account email must be valid")
        String email,

        String idToken
) {}
