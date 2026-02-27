package com.xparience.otp;

public interface SmsSender {
    void sendVerificationCode(String phoneNumber, String message);
}
