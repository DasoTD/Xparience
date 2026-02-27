package com.xparience.otp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultSmsSender implements SmsSender {

    private final SmsSenderProperties properties;

    @Override
    public void sendVerificationCode(String phoneNumber, String message) {
        if ("twilio".equals(properties.resolvedProvider())) {
            sendWithTwilio(phoneNumber, message);
            return;
        }

        log.info("SMS provider=log | to={} | message={}", phoneNumber, message);
    }

    private void sendWithTwilio(String phoneNumber, String message) {
        if (isBlank(properties.twilioAccountSid())
                || isBlank(properties.twilioAuthToken())
                || isBlank(properties.twilioFromNumber())) {
            throw new RuntimeException("Twilio SMS is selected but credentials are missing");
        }

        try {
            String accountSid = properties.twilioAccountSid();
            String auth = Base64.getEncoder().encodeToString(
                    (accountSid + ":" + properties.twilioAuthToken()).getBytes(StandardCharsets.UTF_8));

            String payload = "From=" + urlEncode(properties.twilioFromNumber())
                    + "&To=" + urlEncode(phoneNumber)
                    + "&Body=" + urlEncode(message);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json"))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("Twilio SMS send failed: status={} body={}", response.statusCode(), response.body());
                throw new RuntimeException("Failed to send SMS OTP");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to send SMS OTP", ex);
        } catch (IOException ex) {
            throw new RuntimeException("Failed to send SMS OTP", ex);
        }
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
