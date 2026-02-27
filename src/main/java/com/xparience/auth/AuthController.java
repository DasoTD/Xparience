package com.xparience.auth;

import com.xparience.auth.dto.*;
import com.xparience.common.ApiResponse;
import com.xparience.otp.dto.OtpDispatchResponse;
import com.xparience.otp.dto.OtpVerificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthService authService;
    private final AuthSecurityService authSecurityService;

    public AuthController(AuthService authService, AuthSecurityService authSecurityService) {
        this.authService = authService;
        this.authSecurityService = authSecurityService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user account")
    public ResponseEntity<ApiResponse<OtpDispatchResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-Captcha-Token", required = false) String captchaToken,
            HttpServletRequest servletRequest) {
        String ipAddress = extractClientIp(servletRequest);
        authSecurityService.preCheck(ipAddress, captchaToken);
        try {
            OtpDispatchResponse response = authService.register(request);
            authSecurityService.recordSuccess(ipAddress);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Account created", response));
        } catch (RuntimeException ex) {
            authSecurityService.recordFailure(ipAddress);
            throw ex;
        }
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email or phone with OTP code")
    public ResponseEntity<ApiResponse<OtpVerificationResponse>> verifyEmail(
            @Valid @RequestBody VerifyOtpRequest request,
            @RequestHeader(value = "X-Captcha-Token", required = false) String captchaToken,
            HttpServletRequest servletRequest) {
        String ipAddress = extractClientIp(servletRequest);
        authSecurityService.preCheck(ipAddress, captchaToken);
        try {
            OtpVerificationResponse response = authService.verifyEmail(request);
            authSecurityService.recordSuccess(ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Verification successful", response));
        } catch (RuntimeException ex) {
            authSecurityService.recordFailure(ipAddress);
            throw ex;
        }
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend verification OTP by email or phone")
    public ResponseEntity<ApiResponse<OtpDispatchResponse>> resendOtp(
            @RequestParam String identifier,
            HttpServletRequest servletRequest) {
        String ipAddress = extractClientIp(servletRequest);
        authSecurityService.preCheck(ipAddress, null);
        OtpDispatchResponse response = authService.resendOtp(identifier);
        return ResponseEntity.ok(ApiResponse.success("OTP resent", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Captcha-Token", required = false) String captchaToken,
            HttpServletRequest servletRequest) {
        String ipAddress = extractClientIp(servletRequest);
        authSecurityService.preCheck(ipAddress, captchaToken);
        try {
            AuthResponse response = authService.login(request);
            authSecurityService.recordSuccess(ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (RuntimeException ex) {
            authSecurityService.recordFailure(ipAddress);
            throw ex;
        }
    }

    @PostMapping("/oauth/google")
    @Operation(summary = "Login or sign up with Google OAuth")
    public ResponseEntity<ApiResponse<AuthResponse>> googleOAuthLogin(
            @Valid @RequestBody GoogleOAuthRequest request,
            @RequestHeader(value = "X-Captcha-Token", required = false) String captchaToken,
            HttpServletRequest servletRequest) {
        String ipAddress = extractClientIp(servletRequest);
        authSecurityService.preCheck(ipAddress, captchaToken);
        try {
            AuthResponse response = authService.loginWithGoogle(request);
            authSecurityService.recordSuccess(ipAddress);
            return ResponseEntity.ok(ApiResponse.success("Google authentication successful", response));
        } catch (RuntimeException ex) {
            authSecurityService.recordFailure(ipAddress);
            throw ex;
        }
    }

    @PostMapping("/2fa/verify")
    @Operation(summary = "Verify 2FA OTP to complete login")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyTwoFactor(
            @Valid @RequestBody VerifyTwoFactorRequest request,
            @RequestHeader(value = "X-Captcha-Token", required = false) String captchaToken,
            HttpServletRequest servletRequest) {
        String ipAddress = extractClientIp(servletRequest);
        authSecurityService.preCheck(ipAddress, captchaToken);
        try {
            AuthResponse response = authService.verifyTwoFactorLogin(request);
            authSecurityService.recordSuccess(ipAddress);
            return ResponseEntity.ok(ApiResponse.success("2FA verification successful", response));
        } catch (RuntimeException ex) {
            authSecurityService.recordFailure(ipAddress);
            throw ex;
        }
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Request a password reset OTP")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String message = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        String message = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}