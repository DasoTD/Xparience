package com.xparience.settings;

import com.xparience.profile.ProfileRepository;
import com.xparience.settings.dto.ChangePasswordRequest;
import com.xparience.settings.dto.SettingsSummaryResponse;
import com.xparience.subscription.SubscriptionPlan;
import com.xparience.subscription.SubscriptionRepository;
import com.xparience.subscription.SubscriptionStatus;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import com.xparience.verification.VerificationRepository;
import com.xparience.verification.VerificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final VerificationRepository verificationRepository;
    private final PasswordEncoder passwordEncoder;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public SettingsSummaryResponse getSettingsSummary() {
        User user = getCurrentUser();

        var profile = profileRepository.findByUserId(user.getId()).orElse(null);

        VerificationStatus verificationStatus = verificationRepository
                .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .map(v -> v.getStatus())
                .orElse(VerificationStatus.PENDING);

        SubscriptionPlan currentPlan = subscriptionRepository
                .findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
                .map(s -> s.getPlan())
                .orElse(SubscriptionPlan.FREE);

        SettingsSummaryResponse response = new SettingsSummaryResponse();
        response.setEmail(user.getEmail());
        response.setFullName(profile != null ? profile.getFullName() : null);
        response.setProfilePictureUrl(profile != null ? profile.getProfilePictureUrl() : null);
        response.setVerificationStatus(verificationStatus);
        response.setCurrentPlan(currentPlan);
        response.setEmailVerified(user.isEmailVerified());
        response.setTwoFactorEnabled(user.isTwoFactorEnabled());
        return response;
    }

    @Transactional
    public String toggleTwoFactor(boolean enabled) {
        User user = getCurrentUser();
        user.setTwoFactorEnabled(enabled);
        userRepository.save(user);
        return enabled ? "Two-factor authentication enabled" : "Two-factor authentication disabled";
    }

    @Transactional
    public String changePassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }

        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new RuntimeException("New passwords do not match");
        }

        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new RuntimeException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        return "Password changed successfully";
    }

    @Transactional
    public String logOut() {
        // JWT is stateless — client should discard the token
        // Optionally: implement a token blacklist here
        return "Logged out successfully";
    }
}