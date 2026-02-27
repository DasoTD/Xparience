package com.xparience.chat.message.dto;

public class ToneEnhancerResponse {

    private String originalMessage;
    private String targetTone;
    private String enhancedMessage;

    public String getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
    }

    public String getTargetTone() {
        return targetTone;
    }

    public void setTargetTone(String targetTone) {
        this.targetTone = targetTone;
    }

    public String getEnhancedMessage() {
        return enhancedMessage;
    }

    public void setEnhancedMessage(String enhancedMessage) {
        this.enhancedMessage = enhancedMessage;
    }
}
