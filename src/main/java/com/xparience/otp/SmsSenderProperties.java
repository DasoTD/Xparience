package com.xparience.otp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.sms")
public record SmsSenderProperties(
        String provider,
        String twilioAccountSid,
        String twilioAuthToken,
        String twilioFromNumber
) {
    public String resolvedProvider() {
        return provider == null ? "log" : provider.trim().toLowerCase();
    }
}
