package com.xparience.date.dto;

import com.xparience.date.DateStatus;
import com.xparience.date.DateType;
import com.xparience.date.StreamingPlatform;

import java.time.LocalDateTime;

public class DateInviteResponse {
    private Long inviteId;
    private Long senderId;
    private String senderName;
    private Long recipientId;
    private String recipientName;
    private DateType dateType;
    private StreamingPlatform streamingPlatform;
    private String title;
    private String description;
    private String contentLink;
    private String streamingContentId;
    private DateStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime dateStartedAt;
    private LocalDateTime dateEndedAt;
    private String webrtcRoomId;
    private boolean screenshotBlockingRequired;
    private int rescheduleCount;
    private LocalDateTime createdAt;

    public Long getInviteId() {
        return inviteId;
    }

    public void setInviteId(Long inviteId) {
        this.inviteId = inviteId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
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

    public DateStatus getStatus() {
        return status;
    }

    public void setStatus(DateStatus status) {
        this.status = status;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public LocalDateTime getDateStartedAt() {
        return dateStartedAt;
    }

    public void setDateStartedAt(LocalDateTime dateStartedAt) {
        this.dateStartedAt = dateStartedAt;
    }

    public LocalDateTime getDateEndedAt() {
        return dateEndedAt;
    }

    public void setDateEndedAt(LocalDateTime dateEndedAt) {
        this.dateEndedAt = dateEndedAt;
    }

    public String getWebrtcRoomId() {
        return webrtcRoomId;
    }

    public void setWebrtcRoomId(String webrtcRoomId) {
        this.webrtcRoomId = webrtcRoomId;
    }

    public boolean isScreenshotBlockingRequired() {
        return screenshotBlockingRequired;
    }

    public void setScreenshotBlockingRequired(boolean screenshotBlockingRequired) {
        this.screenshotBlockingRequired = screenshotBlockingRequired;
    }

    public int getRescheduleCount() {
        return rescheduleCount;
    }

    public void setRescheduleCount(int rescheduleCount) {
        this.rescheduleCount = rescheduleCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}