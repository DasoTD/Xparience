package com.xparience.settings.dto;

import com.xparience.subscription.SubscriptionPlan;
import com.xparience.verification.VerificationStatus;

public class SettingsSummaryResponse {
    private String email;
    private String fullName;
    private String profilePictureUrl;
    private VerificationStatus verificationStatus;
    private SubscriptionPlan currentPlan;
    private boolean emailVerified;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public VerificationStatus getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(VerificationStatus verificationStatus) { this.verificationStatus = verificationStatus; }
    public SubscriptionPlan getCurrentPlan() { return currentPlan; }
    public void setCurrentPlan(SubscriptionPlan currentPlan) { this.currentPlan = currentPlan; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
}
