package com.xparience.date.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class RescheduleDateRequest {

    @NotNull(message = "New schedule time is required")
    private LocalDateTime newScheduledAt;

    public LocalDateTime getNewScheduledAt() {
        return newScheduledAt;
    }

    public void setNewScheduledAt(LocalDateTime newScheduledAt) {
        this.newScheduledAt = newScheduledAt;
    }
}
