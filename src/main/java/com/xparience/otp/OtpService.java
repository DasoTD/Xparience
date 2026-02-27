package com.xparience.otp;

import com.xparience.otp.dto.OtpDispatchResponse;
import com.xparience.otp.dto.OtpVerificationResponse;
import com.xparience.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_OTP_ATTEMPTS = 3;

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;
    private final SmsSender smsSender;

    @Transactional
    public OtpDispatchResponse generateAndSendOtp(User user, OtpType type) {
        return generateAndSendOtp(user, type, false);
    }

    @Transactional
    public OtpDispatchResponse generateAndSendOtp(User user, OtpType type, boolean sendViaPhone) {
        otpRepository.findTopByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), type)
                .ifPresent(existing -> {
                    long secondsSinceCreation = ChronoUnit.SECONDS.between(existing.getCreatedAt(), LocalDateTime.now());
                    long remaining = RESEND_COOLDOWN_SECONDS - secondsSinceCreation;
                    if (remaining > 0) {
                        throw new RuntimeException("Resend available in " + remaining + " seconds. resendAvailableInSeconds=" + remaining);
                    }
                });

        otpRepository.deleteAllByUserIdAndType(user.getId(), type);

        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpToken token = new OtpToken();
        token.setToken(otp);
        token.setType(type);
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        token.setAttemptsRemaining(MAX_OTP_ATTEMPTS);

        otpRepository.save(token);
        sendOtp(user, otp, type, sendViaPhone);

        OtpDispatchResponse response = new OtpDispatchResponse();
        response.setMessage(sendViaPhone
            ? "Verification code sent to phone"
            : "Verification code sent to email");
        response.setChannel(sendViaPhone ? "SMS" : "EMAIL");
        response.setExpiresInSeconds(OTP_EXPIRY_MINUTES * 60L);
        response.setResendAvailableInSeconds(RESEND_COOLDOWN_SECONDS);
        return response;
    }

    private void sendOtp(User user, String otp, OtpType type, boolean sendViaPhone) {
        if (sendViaPhone) {
            smsSender.sendVerificationCode(user.getPhoneNumber(), "Your Xparience verification code is: " + otp + ". Expires in 5 minutes.");
            return;
        }

        sendOtpEmail(user.getEmail(), otp, type);
    }

    @Async
    protected void sendOtpEmail(String email, String otp, OtpType type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);

        if (type == OtpType.EMAIL_VERIFICATION) {
            message.setSubject("Verify your Xparience account");
            message.setText("Your verification code is: " + otp + "\n\nThis code expires in 5 minutes.");
        } else if (type == OtpType.PASSWORD_RESET) {
            message.setSubject("Reset your Xparience password");
            message.setText("Your password reset code is: " + otp + "\n\nThis code expires in 5 minutes.");
        } else {
            message.setSubject("Your Xparience login verification code");
            message.setText("Your 2FA login code is: " + otp + "\n\nThis code expires in 5 minutes.");
        }
        
            // mailSender.send(message);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send OTP email to {}: {}", email, e.getMessage());
        }
    }

    @Transactional
    public OtpVerificationResponse verifyOtp(User user, String otp, OtpType type) {
        OtpToken latestToken = otpRepository.findTopByUserIdAndTypeOrderByCreatedAtDesc(user.getId(), type)
                .orElseThrow(() -> new RuntimeException("No OTP found. Please request a new code."));

        if (latestToken.isExpired()) {
            throw new RuntimeException("OTP code has expired. Please request a new one.");
        }

        if (latestToken.getConfirmedAt() != null) {
            throw new RuntimeException("OTP code has already been used.");
        }

        if (!latestToken.getToken().equals(otp)) {
            int remaining = Math.max(0, latestToken.getAttemptsRemaining() - 1);
            latestToken.setAttemptsRemaining(remaining);
            otpRepository.save(latestToken);

            if (remaining == 0) {
                throw new RuntimeException("Maximum OTP attempts exceeded. Please request a new code. attemptsRemaining=0");
            }

            throw new RuntimeException("Incorrect OTP. " + remaining + " attempt(s) remaining. attemptsRemaining=" + remaining);
        }

        latestToken.setConfirmedAt(LocalDateTime.now());
        otpRepository.save(latestToken);
        OtpVerificationResponse response = new OtpVerificationResponse();
        response.setMessage("OTP verified successfully");
        response.setVerified(true);
        response.setAttemptsRemaining(latestToken.getAttemptsRemaining());
        return response;
    }

    @Transactional
    public OtpVerificationResponse verifyOtp(String otp, OtpType type) {
        OtpToken token = otpRepository.findByTokenAndType(otp, type)
                .orElseThrow(() -> new RuntimeException("Invalid OTP code"));

        if (token.isExpired()) {
            throw new RuntimeException("OTP code has expired. Please request a new one.");
        }

        if (token.getConfirmedAt() != null) {
            throw new RuntimeException("OTP code has already been used.");
        }

        token.setConfirmedAt(LocalDateTime.now());
        otpRepository.save(token);
        OtpVerificationResponse response = new OtpVerificationResponse();
        response.setMessage("OTP verified successfully");
        response.setVerified(true);
        response.setAttemptsRemaining(token.getAttemptsRemaining() == null ? 0 : token.getAttemptsRemaining());
        return response;
    }
}