package com.xparience.date.dto;

import java.time.LocalDateTime;

public class VideoRoomResponse {
    private Long inviteId;
    private String roomId;
    private String joinToken;
    private LocalDateTime roomCreatedAt;
    private boolean screenshotBlockingRequired;

    public Long getInviteId() {
        return inviteId;
    }

    public void setInviteId(Long inviteId) {
        this.inviteId = inviteId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getJoinToken() {
        return joinToken;
    }

    public void setJoinToken(String joinToken) {
        this.joinToken = joinToken;
    }

    public LocalDateTime getRoomCreatedAt() {
        return roomCreatedAt;
    }

    public void setRoomCreatedAt(LocalDateTime roomCreatedAt) {
        this.roomCreatedAt = roomCreatedAt;
    }

    public boolean isScreenshotBlockingRequired() {
        return screenshotBlockingRequired;
    }

    public void setScreenshotBlockingRequired(boolean screenshotBlockingRequired) {
        this.screenshotBlockingRequired = screenshotBlockingRequired;
    }
}
