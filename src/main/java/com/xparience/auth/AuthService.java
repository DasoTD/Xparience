package com.xparience.auth;

import com.xparience.auth.dto.*;
import com.xparience.config.JwtService;
import com.xparience.otp.OtpService;
import com.xparience.otp.OtpType;
import com.xparience.otp.dto.OtpDispatchResponse;
import com.xparience.otp.dto.OtpVerificationResponse;
import com.xparience.profile.Profile;
import com.xparience.profile.ProfileRepository;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";
    private static final String E164_PHONE_REGEX = "^\\+[1-9]\\d{1,14}$";
    private static final String UK_PHONE_REGEX = "^\\+44\\d{10}$";

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Transactional
    public OtpDispatchResponse register(RegisterRequest request) {
        String normalizedEmail = normalize(request.email());
        String normalizedPhone = normalize(request.phoneNumber());

        if (isBlank(normalizedEmail) && isBlank(normalizedPhone)) {
            throw new RuntimeException("Provide either an email or a phone number to register");
        }

        if (!isBlank(normalizedEmail)) {
            validateEmail(normalizedEmail);
            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new RuntimeException("Account already exists. Login or reset password?");
            }
        }

        if (!isBlank(normalizedPhone)) {
            validatePhone(normalizedPhone);
            if (userRepository.existsByPhoneNumber(normalizedPhone)) {
                throw new RuntimeException("Account already exists. Login or reset password?");
            }
        }

        if (!request.password().matches(PASSWORD_REGEX)) {
            throw new RuntimeException("Password must be at least 8 characters and include uppercase, lowercase, number, and special character");
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        if (request.dateOfBirth() == null) {
            throw new RuntimeException("Date of birth is required");
        }

        if (request.dateOfBirth().isAfter(LocalDate.now())) {
            throw new RuntimeException("Date of birth cannot be in the future");
        }

        if (Period.between(request.dateOfBirth(), LocalDate.now()).getYears() < 25) {
            throw new RuntimeException("You must be at least 25 years old to register");
        }

        if (!request.termsAccepted() || !request.privacyPolicyAccepted()) {
            throw new RuntimeException("You must accept Terms of Service and Privacy Policy to continue");
        }

        User user = new User();
        user.setEmail(isBlank(normalizedEmail) ? normalizedPhone + "@phone.local" : normalizedEmail);
        user.setPhoneNumber(normalizedPhone);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setEmailVerified(false);

        userRepository.save(user);

        OtpType otpType = OtpType.EMAIL_VERIFICATION;
        boolean sendViaPhone = isBlank(normalizedEmail) && !isBlank(normalizedPhone);
        OtpDispatchResponse response = otpService.generateAndSendOtp(user, otpType, sendViaPhone);
        response.setMessage(isBlank(normalizedPhone)
            ? "Account created! Please check your email for the verification code."
            : "Account created! Please check your phone for the verification code.");
        return response;
    }

    @Transactional
        public OtpVerificationResponse verifyEmail(VerifyOtpRequest request) {
        String identifier = normalize(request.getIdentifier());
        if (isBlank(identifier)) {
            throw new RuntimeException("Email or phone number is required");
        }

        User user = resolveUserByIdentifier(identifier);
        OtpType otpType = OtpType.EMAIL_VERIFICATION;

        OtpVerificationResponse response = otpService.verifyOtp(user, request.getOtp(), otpType);

        user.setEmailVerified(true);
        userRepository.save(user);

        response.setMessage("Email verified successfully! You can now complete your profile.");
        return response;
    }

    @Transactional
    public OtpDispatchResponse resendOtp(String identifier) {
        User user = resolveUserByIdentifier(identifier);

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }

        OtpType otpType = OtpType.EMAIL_VERIFICATION;
        OtpDispatchResponse response = otpService.generateAndSendOtp(user, otpType, isPhone(identifier));
        response.setMessage(isPhone(identifier)
            ? "Verification code resent. Please check your phone."
            : "Verification code resent. Please check your email.");
        return response;
    }

    public AuthResponse login(LoginRequest request) {
        String identifier = normalize(request.getIdentifier());
        User user = resolveUserByIdentifier(identifier);

        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(user.getEmail(), request.getPassword())
        );

        if (!user.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in");
        }

        if (user.isTwoFactorEnabled()) {
            OtpDispatchResponse otpResponse = otpService.generateAndSendOtp(
                    user,
                    OtpType.LOGIN_2FA,
                    !isBlank(user.getPhoneNumber())
            );
            return buildTwoFactorChallengeResponse(user, otpResponse.getChannel());
        }

        return buildAuthenticatedResponse(user);
    }

    @Transactional
    public AuthResponse loginWithGoogle(GoogleOAuthRequest request) {
        String normalizedEmail = normalize(request.email());
        if (isBlank(normalizedEmail)) {
            throw new RuntimeException("Google account email is required");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> {
                    User created = new User();
                    created.setEmail(normalizedEmail);
                    created.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    created.setEmailVerified(true);
                    return userRepository.save(created);
                });

        if (!user.isEnabled()) {
            throw new RuntimeException("Account is suspended");
        }

        if (user.isTwoFactorEnabled()) {
            OtpDispatchResponse otpResponse = otpService.generateAndSendOtp(
                    user,
                    OtpType.LOGIN_2FA,
                    !isBlank(user.getPhoneNumber())
            );
            return buildTwoFactorChallengeResponse(user, otpResponse.getChannel());
        }

        return buildAuthenticatedResponse(user);
    }

    @Transactional
    public AuthResponse verifyTwoFactorLogin(VerifyTwoFactorRequest request) {
        User user = resolveUserByIdentifier(request.identifier());

        if (!user.isTwoFactorEnabled()) {
            throw new RuntimeException("Two-factor authentication is not enabled for this account");
        }

        otpService.verifyOtp(user, request.otp(), OtpType.LOGIN_2FA);
        return buildAuthenticatedResponse(user);
    }

    private AuthResponse buildAuthenticatedResponse(User user) {
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        int lastCompletedStep = getLastCompletedStep(profile);
        int nextStep = Math.min(lastCompletedStep + 1, 8);
        boolean profileComplete = isProfileComplete(profile);

        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setEmail(user.getEmail());
        response.setEmailVerified(user.isEmailVerified());
        response.setProfileComplete(profileComplete);
        response.setRegistrationStatus(profileComplete ? "Active" : "Registration in Progress");
        response.setLastCompletedStep(lastCompletedStep);
        response.setNextStep(nextStep);
        response.setOnboardingRequired(!profileComplete);
        response.setTwoFactorRequired(false);
        response.setTwoFactorChannel(null);
        return response;
    }

    private AuthResponse buildTwoFactorChallengeResponse(User user, String channel) {
        Profile profile = profileRepository.findByUserId(user.getId()).orElse(null);
        int lastCompletedStep = getLastCompletedStep(profile);
        int nextStep = Math.min(lastCompletedStep + 1, 8);
        boolean profileComplete = isProfileComplete(profile);

        AuthResponse response = new AuthResponse();
        response.setAccessToken(null);
        response.setRefreshToken(null);
        response.setTokenType("Bearer");
        response.setEmail(user.getEmail());
        response.setEmailVerified(user.isEmailVerified());
        response.setProfileComplete(profileComplete);
        response.setRegistrationStatus(profileComplete ? "Active" : "Registration in Progress");
        response.setLastCompletedStep(lastCompletedStep);
        response.setNextStep(nextStep);
        response.setOnboardingRequired(!profileComplete);
        response.setTwoFactorRequired(true);
        response.setTwoFactorChannel(channel);
        return response;
    }

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        otpService.generateAndSendOtp(user, OtpType.PASSWORD_RESET, false);
        return "Password reset code sent to your email.";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        otpService.verifyOtp(user, request.otp(), OtpType.PASSWORD_RESET);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return "Password reset successfully. You can now log in.";
    }

    private User resolveUserByIdentifier(String identifier) {
        String normalized = normalize(identifier);
        if (isPhone(normalized)) {
            return userRepository.findByPhoneNumber(normalized)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        return userRepository.findByEmail(normalized)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void validateEmail(String email) {
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
        } catch (Exception ex) {
            throw new RuntimeException("Please provide a valid email address");
        }
    }

    private void validatePhone(String phoneNumber) {
        if (!phoneNumber.matches(E164_PHONE_REGEX)) {
            throw new RuntimeException("Phone number must be in E.164 format");
        }
        if (!phoneNumber.matches(UK_PHONE_REGEX)) {
            throw new RuntimeException("Only UK phone numbers are supported");
        }
    }

    private boolean isProfileComplete(Profile profile) {
        return profile != null
                && profile.isBasicInfoComplete()
                && profile.isImagesComplete()
                && profile.isAboutYouComplete()
                && profile.isPreferencesComplete()
                && profile.isNonNegotiablesComplete()
                && profile.isNutritionVibeComplete()
                && profile.isPersonalityQuizComplete()
                && profile.isReviewSubmitted();
    }

    private int getLastCompletedStep(Profile profile) {
        if (profile == null) {
            return 0;
        }

        int step = 0;
        if (profile.isBasicInfoComplete()) step = 1;
        if (profile.isImagesComplete()) step = 2;
        if (profile.isAboutYouComplete()) step = 3;
        if (profile.isPreferencesComplete()) step = 4;
        if (profile.isNonNegotiablesComplete()) step = 5;
        if (profile.isNutritionVibeComplete()) step = 6;
        if (profile.isPersonalityQuizComplete()) step = 7;
        if (profile.isReviewSubmitted()) step = 8;
        return step;
    }

    private boolean isPhone(String value) {
        return !isBlank(value) && value.startsWith("+");
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}