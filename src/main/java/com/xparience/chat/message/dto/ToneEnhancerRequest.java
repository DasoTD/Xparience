package com.xparience.chat.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ToneEnhancerRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 5000, message = "Message must not exceed 5000 characters")
    private String message;

    @NotBlank(message = "Target tone is required")
    private String targetTone;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTargetTone() {
        return targetTone;
    }

    public void setTargetTone(String targetTone) {
        this.targetTone = targetTone;
    }
}
