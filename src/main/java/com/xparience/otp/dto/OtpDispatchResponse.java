package com.xparience.otp.dto;

public class OtpDispatchResponse {
    private String message;
    private String channel;
    private long expiresInSeconds;
    private long resendAvailableInSeconds;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public long getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public void setExpiresInSeconds(long expiresInSeconds) {
        this.expiresInSeconds = expiresInSeconds;
    }

    public long getResendAvailableInSeconds() {
        return resendAvailableInSeconds;
    }

    public void setResendAvailableInSeconds(long resendAvailableInSeconds) {
        this.resendAvailableInSeconds = resendAvailableInSeconds;
    }
}
