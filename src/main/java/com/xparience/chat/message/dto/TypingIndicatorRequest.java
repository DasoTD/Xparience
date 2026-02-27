package com.xparience.chat.message.dto;

import jakarta.validation.constraints.NotNull;

public class TypingIndicatorRequest {

    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    private boolean typing;

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public boolean isTyping() {
        return typing;
    }

    public void setTyping(boolean typing) {
        this.typing = typing;
    }
}
