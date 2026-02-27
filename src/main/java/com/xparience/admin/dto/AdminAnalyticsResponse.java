package com.xparience.admin.dto;

public class AdminAnalyticsResponse {
    private long totalUsers;
    private long activeUsers;
    private long matchesGenerated;

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public long getActiveUsers() { return activeUsers; }
    public void setActiveUsers(long activeUsers) { this.activeUsers = activeUsers; }
    public long getMatchesGenerated() { return matchesGenerated; }
    public void setMatchesGenerated(long matchesGenerated) { this.matchesGenerated = matchesGenerated; }
}
