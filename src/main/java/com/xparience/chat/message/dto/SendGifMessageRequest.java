package com.xparience.chat.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SendGifMessageRequest {

    @NotNull(message = "Recipient user ID is required")
    private Long recipientUserId;

    @NotBlank(message = "GIF URL is required")
    @Size(max = 2048, message = "GIF URL must not exceed 2048 characters")
    private String gifUrl;

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }
}
