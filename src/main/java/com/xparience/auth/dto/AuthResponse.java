package com.xparience.auth.dto;

public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private String email;
    private boolean emailVerified;
    private boolean profileComplete;
    private String registrationStatus;
    private int lastCompletedStep;
    private int nextStep;
    private boolean onboardingRequired;
    private boolean twoFactorRequired;
    private String twoFactorChannel;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean isProfileComplete() {
        return profileComplete;
    }

    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }

    public String getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public int getLastCompletedStep() {
        return lastCompletedStep;
    }

    public void setLastCompletedStep(int lastCompletedStep) {
        this.lastCompletedStep = lastCompletedStep;
    }

    public int getNextStep() {
        return nextStep;
    }

    public void setNextStep(int nextStep) {
        this.nextStep = nextStep;
    }

    public boolean isOnboardingRequired() {
        return onboardingRequired;
    }

    public void setOnboardingRequired(boolean onboardingRequired) {
        this.onboardingRequired = onboardingRequired;
    }

    public boolean isTwoFactorRequired() {
        return twoFactorRequired;
    }

    public void setTwoFactorRequired(boolean twoFactorRequired) {
        this.twoFactorRequired = twoFactorRequired;
    }

    public String getTwoFactorChannel() {
        return twoFactorChannel;
    }

    public void setTwoFactorChannel(String twoFactorChannel) {
        this.twoFactorChannel = twoFactorChannel;
    }
}
