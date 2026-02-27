package com.xparience.date.dto;

import com.xparience.date.DateType;
import com.xparience.date.StreamingPlatform;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CreateDateInviteRequest {

    @NotNull(message = "Recipient user ID is required")
    private Long recipientUserId;

    @NotNull(message = "Date type is required")
    private DateType dateType;

    private StreamingPlatform streamingPlatform;
    private String title;
    private String description;
    private String contentLink;
    private String streamingContentId;
    private Boolean screenshotBlockingRequired;
    private LocalDateTime scheduledAt;

    public Long getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(Long recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public DateType getDateType() {
        return dateType;
    }

    public void setDateType(DateType dateType) {
        this.dateType = dateType;
    }

    public StreamingPlatform getStreamingPlatform() {
        return streamingPlatform;
    }

    public void setStreamingPlatform(StreamingPlatform streamingPlatform) {
        this.streamingPlatform = streamingPlatform;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContentLink() {
        return contentLink;
    }

    public void setContentLink(String contentLink) {
        this.contentLink = contentLink;
    }

    public String getStreamingContentId() {
        return streamingContentId;
    }

    public void setStreamingContentId(String streamingContentId) {
        this.streamingContentId = streamingContentId;
    }

    public Boolean getScreenshotBlockingRequired() {
        return screenshotBlockingRequired;
    }

    public void setScreenshotBlockingRequired(Boolean screenshotBlockingRequired) {
        this.screenshotBlockingRequired = screenshotBlockingRequired;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }
}
