package com.xparience.profile.dto;

public class ProfileCompletionResponse {
    private boolean basicInfoComplete;
    private boolean imagesComplete;
    private boolean aboutYouComplete;
    private boolean preferencesComplete;
    private boolean nonNegotiablesComplete;
    private boolean nutritionVibeComplete;
    private boolean personalityQuizComplete;
    private boolean reviewSubmitted;
    private boolean identityVerified;
    private int completionPercentage;
    private String registrationStatus;
    private int lastCompletedStep;
    private int nextStep;
    private boolean onboardingRequired;
    private String resumeMessage;

    public boolean isBasicInfoComplete() { return basicInfoComplete; }
    public void setBasicInfoComplete(boolean basicInfoComplete) { this.basicInfoComplete = basicInfoComplete; }
    public boolean isImagesComplete() { return imagesComplete; }
    public void setImagesComplete(boolean imagesComplete) { this.imagesComplete = imagesComplete; }
    public boolean isAboutYouComplete() { return aboutYouComplete; }
    public void setAboutYouComplete(boolean aboutYouComplete) { this.aboutYouComplete = aboutYouComplete; }
    public boolean isPreferencesComplete() { return preferencesComplete; }
    public void setPreferencesComplete(boolean preferencesComplete) { this.preferencesComplete = preferencesComplete; }
    public boolean isNonNegotiablesComplete() { return nonNegotiablesComplete; }
    public void setNonNegotiablesComplete(boolean nonNegotiablesComplete) { this.nonNegotiablesComplete = nonNegotiablesComplete; }
    public boolean isNutritionVibeComplete() { return nutritionVibeComplete; }
    public void setNutritionVibeComplete(boolean nutritionVibeComplete) { this.nutritionVibeComplete = nutritionVibeComplete; }
    public boolean isPersonalityQuizComplete() { return personalityQuizComplete; }
    public void setPersonalityQuizComplete(boolean personalityQuizComplete) { this.personalityQuizComplete = personalityQuizComplete; }
    public boolean isReviewSubmitted() { return reviewSubmitted; }
    public void setReviewSubmitted(boolean reviewSubmitted) { this.reviewSubmitted = reviewSubmitted; }
    public boolean isIdentityVerified() { return identityVerified; }
    public void setIdentityVerified(boolean identityVerified) { this.identityVerified = identityVerified; }
    public int getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(int completionPercentage) { this.completionPercentage = completionPercentage; }
    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
    public int getLastCompletedStep() { return lastCompletedStep; }
    public void setLastCompletedStep(int lastCompletedStep) { this.lastCompletedStep = lastCompletedStep; }
    public int getNextStep() { return nextStep; }
    public void setNextStep(int nextStep) { this.nextStep = nextStep; }
    public boolean isOnboardingRequired() { return onboardingRequired; }
    public void setOnboardingRequired(boolean onboardingRequired) { this.onboardingRequired = onboardingRequired; }
    public String getResumeMessage() { return resumeMessage; }
    public void setResumeMessage(String resumeMessage) { this.resumeMessage = resumeMessage; }
}
