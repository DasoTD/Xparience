package com.xparience.chat.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReportConversationRequest {

    @NotBlank(message = "Report reason is required")
    @Size(max = 120, message = "Report reason must not exceed 120 characters")
    private String reason;

    @Size(max = 1000, message = "Report details must not exceed 1000 characters")
    private String details;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
