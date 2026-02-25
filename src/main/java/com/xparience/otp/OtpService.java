package com.xparience.otp;

import com.xparience.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    @Transactional
    public String generateAndSendOtp(User user, OtpType type) {
        // Delete any existing OTPs of this type for the user
        otpRepository.deleteAllByUserIdAndType(user.getId(), type);

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        OtpToken token = new OtpToken();
        token.setToken(otp);
        token.setType(type);
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        otpRepository.save(token);
        sendOtpEmail(user.getEmail(), otp, type);

        return otp;
    }

    @Async
    protected void sendOtpEmail(String email, String otp, OtpType type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);

        if (type == OtpType.EMAIL_VERIFICATION) {
            message.setSubject("Verify your Xparience account");
            message.setText("Your verification code is: " + otp + "\n\nThis code expires in 15 minutes.");
        } else {
            message.setSubject("Reset your Xparience password");
            message.setText("Your password reset code is: " + otp + "\n\nThis code expires in 15 minutes.");
        }
        
            // mailSender.send(message);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            log.warn("Failed to send OTP email to {}: {}", email, e.getMessage());
        }
    }

    @Transactional
    public boolean verifyOtp(String otp, OtpType type) {
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
        return true;
    }
}