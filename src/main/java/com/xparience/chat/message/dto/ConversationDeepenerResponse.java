package com.xparience.chat.message.dto;

import java.util.List;

public class ConversationDeepenerResponse {

    private Long conversationId;
    private String anchorTopic;
    private List<String> suggestedPrompts;

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getAnchorTopic() {
        return anchorTopic;
    }

    public void setAnchorTopic(String anchorTopic) {
        this.anchorTopic = anchorTopic;
    }

    public List<String> getSuggestedPrompts() {
        return suggestedPrompts;
    }

    public void setSuggestedPrompts(List<String> suggestedPrompts) {
        this.suggestedPrompts = suggestedPrompts;
    }
}
