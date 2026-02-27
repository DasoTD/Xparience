package com.xparience.date.dto;

public class PostDateAnalyticsResponse {
    private Long inviteId;
    private long durationMinutes;
    private long syncEvents;
    private long pauseEvents;
    private long seekEvents;

    public Long getInviteId() {
        return inviteId;
    }

    public void setInviteId(Long inviteId) {
        this.inviteId = inviteId;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public long getSyncEvents() {
        return syncEvents;
    }

    public void setSyncEvents(long syncEvents) {
        this.syncEvents = syncEvents;
    }

    public long getPauseEvents() {
        return pauseEvents;
    }

    public void setPauseEvents(long pauseEvents) {
        this.pauseEvents = pauseEvents;
    }

    public long getSeekEvents() {
        return seekEvents;
    }

    public void setSeekEvents(long seekEvents) {
        this.seekEvents = seekEvents;
    }
}
