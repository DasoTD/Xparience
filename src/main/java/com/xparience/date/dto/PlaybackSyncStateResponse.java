package com.xparience.date.dto;

import java.time.LocalDateTime;

public class PlaybackSyncStateResponse {
    private Long inviteId;
    private String lastAction;
    private Long positionSeconds;
    private LocalDateTime lastSyncedAt;

    public Long getInviteId() {
        return inviteId;
    }

    public void setInviteId(Long inviteId) {
        this.inviteId = inviteId;
    }

    public String getLastAction() {
        return lastAction;
    }

    public void setLastAction(String lastAction) {
        this.lastAction = lastAction;
    }

    public Long getPositionSeconds() {
        return positionSeconds;
    }

    public void setPositionSeconds(Long positionSeconds) {
        this.positionSeconds = positionSeconds;
    }

    public LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
