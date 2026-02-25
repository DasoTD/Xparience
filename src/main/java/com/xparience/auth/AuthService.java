package com.xparience.auth;

import com.xparience.auth.dto.*;
import com.xparience.config.JwtService;
import com.xparience.otp.OtpService;
import com.xparience.otp.OtpType;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Transactional
    public String register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("An account with this email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmailVerified(false);

        userRepository.save(user);
        otpService.generateAndSendOtp(user, OtpType.EMAIL_VERIFICATION);

        return "Account created! Please check your email for the verification code.";
    }

    @Transactional
    public String verifyEmail(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        otpService.verifyOtp(request.getOtp(), OtpType.EMAIL_VERIFICATION);

        user.setEmailVerified(true);
        userRepository.save(user);

        return "Email verified successfully! You can now complete your profile.";
    }

    @Transactional
    public String resendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        otpService.generateAndSendOtp(user, OtpType.EMAIL_VERIFICATION);
        return "Verification code resent. Please check your email.";
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setEmail(user.getEmail());
        response.setEmailVerified(user.isEmailVerified());
        response.setProfileComplete(false);
        return response;
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        otpService.generateAndSendOtp(user, OtpType.PASSWORD_RESET);
        return "Password reset code sent to your email.";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        otpService.verifyOtp(request.otp(), OtpType.PASSWORD_RESET);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return "Password reset successfully. You can now log in.";
    }
}