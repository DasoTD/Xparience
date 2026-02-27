package com.xparience.chat.message.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ConversationDeepenerRequest {

    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @Size(max = 120, message = "Context topic must not exceed 120 characters")
    private String contextTopic;

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getContextTopic() {
        return contextTopic;
    }

    public void setContextTopic(String contextTopic) {
        this.contextTopic = contextTopic;
    }
}
