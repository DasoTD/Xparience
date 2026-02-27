package com.xparience.profile.dto;

import jakarta.validation.constraints.AssertTrue;

public record ReviewSubmitRequest(
        @AssertTrue(message = "Profile review must be confirmed before submission")
        boolean confirmed
) {
}
