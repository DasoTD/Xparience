package com.xparience.chat.conversation.dto;

import java.time.LocalDateTime;

public class ConversationResponse {
    private Long conversationId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfilePicture;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private int unreadCount;
    private boolean isBlocked;

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserProfilePicture() {
        return otherUserProfilePicture;
    }

    public void setOtherUserProfilePicture(String otherUserProfilePicture) {
        this.otherUserProfilePicture = otherUserProfilePicture;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }
}
