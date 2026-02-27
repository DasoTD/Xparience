package com.xparience.date.dto;

import com.xparience.date.SyncAction;
import jakarta.validation.constraints.NotNull;

public class PlaybackSyncRequest {

    @NotNull(message = "Sync action is required")
    private SyncAction action;

    private Long positionSeconds;

    public SyncAction getAction() {
        return action;
    }

    public void setAction(SyncAction action) {
        this.action = action;
    }

    public Long getPositionSeconds() {
        return positionSeconds;
    }

    public void setPositionSeconds(Long positionSeconds) {
        this.positionSeconds = positionSeconds;
    }
}
