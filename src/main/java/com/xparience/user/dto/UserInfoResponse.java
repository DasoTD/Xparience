package com.xparience.user.dto;

import java.time.LocalDate;
import java.util.List;

public class UserInfoResponse {
    private Long userId;
    private String email;
    private String fullName;
    private LocalDate dateOfBirth;
    private String genderIdentity;
    private String genderPreference;
    private String relationshipGoal;
    private String reasonForJoining;
    private String city;
    private String profilePictureUrl;
    private List<String> additionalImageUrls;
    private String bio;
    private String values;
    private List<String> preferences;
    private String nonNegotiable1;
    private String nonNegotiable2;
    private String nonNegotiable3;
    private String dietStyle;
    private String healthGoals;
    private String allergiesOrRestrictions;
    private String idealWeekendActivity;
    private String fictionalDinnerGuest;
    private String threeWordsFromFriend;
    private String surprisingPassion;
    private String emotionalIntelligence;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getGenderIdentity() { return genderIdentity; }
    public void setGenderIdentity(String genderIdentity) { this.genderIdentity = genderIdentity; }
    public String getGenderPreference() { return genderPreference; }
    public void setGenderPreference(String genderPreference) { this.genderPreference = genderPreference; }
    public String getRelationshipGoal() { return relationshipGoal; }
    public void setRelationshipGoal(String relationshipGoal) { this.relationshipGoal = relationshipGoal; }
    public String getReasonForJoining() { return reasonForJoining; }
    public void setReasonForJoining(String reasonForJoining) { this.reasonForJoining = reasonForJoining; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public List<String> getAdditionalImageUrls() { return additionalImageUrls; }
    public void setAdditionalImageUrls(List<String> additionalImageUrls) { this.additionalImageUrls = additionalImageUrls; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getValues() { return values; }
    public void setValues(String values) { this.values = values; }
    public List<String> getPreferences() { return preferences; }
    public void setPreferences(List<String> preferences) { this.preferences = preferences; }
    public String getNonNegotiable1() { return nonNegotiable1; }
    public void setNonNegotiable1(String nonNegotiable1) { this.nonNegotiable1 = nonNegotiable1; }
    public String getNonNegotiable2() { return nonNegotiable2; }
    public void setNonNegotiable2(String nonNegotiable2) { this.nonNegotiable2 = nonNegotiable2; }
    public String getNonNegotiable3() { return nonNegotiable3; }
    public void setNonNegotiable3(String nonNegotiable3) { this.nonNegotiable3 = nonNegotiable3; }
    public String getDietStyle() { return dietStyle; }
    public void setDietStyle(String dietStyle) { this.dietStyle = dietStyle; }
    public String getHealthGoals() { return healthGoals; }
    public void setHealthGoals(String healthGoals) { this.healthGoals = healthGoals; }
    public String getAllergiesOrRestrictions() { return allergiesOrRestrictions; }
    public void setAllergiesOrRestrictions(String allergiesOrRestrictions) { this.allergiesOrRestrictions = allergiesOrRestrictions; }
    public String getIdealWeekendActivity() { return idealWeekendActivity; }
    public void setIdealWeekendActivity(String idealWeekendActivity) { this.idealWeekendActivity = idealWeekendActivity; }
    public String getFictionalDinnerGuest() { return fictionalDinnerGuest; }
    public void setFictionalDinnerGuest(String fictionalDinnerGuest) { this.fictionalDinnerGuest = fictionalDinnerGuest; }
    public String getThreeWordsFromFriend() { return threeWordsFromFriend; }
    public void setThreeWordsFromFriend(String threeWordsFromFriend) { this.threeWordsFromFriend = threeWordsFromFriend; }
    public String getSurprisingPassion() { return surprisingPassion; }
    public void setSurprisingPassion(String surprisingPassion) { this.surprisingPassion = surprisingPassion; }
    public String getEmotionalIntelligence() { return emotionalIntelligence; }
    public void setEmotionalIntelligence(String emotionalIntelligence) { this.emotionalIntelligence = emotionalIntelligence; }
}