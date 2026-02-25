package com.xparience.profile;

import com.xparience.user.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String fullName;
    private LocalDate dateOfBirth;
    private String genderIdentity;
    private String genderPreference;
    private String relationshipGoal;
    private String reasonForJoining;
    private String city;
    private String profilePictureUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String values;

    @Transient
    private List<String> additionalImageUrls;

    @Transient
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

    private boolean basicInfoComplete = false;
    private boolean imagesComplete = false;
    private boolean aboutYouComplete = false;
    private boolean preferencesComplete = false;
    private boolean nonNegotiablesComplete = false;
    private boolean nutritionVibeComplete = false;
    private boolean personalityQuizComplete = false;
    private boolean identityVerified = false;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = LocalDateTime.now(); }

    // Getters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getFullName() { return fullName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGenderIdentity() { return genderIdentity; }
    public String getGenderPreference() { return genderPreference; }
    public String getRelationshipGoal() { return relationshipGoal; }
    public String getReasonForJoining() { return reasonForJoining; }
    public String getCity() { return city; }
    public String getProfilePictureUrl() { return profilePictureUrl; }
    public String getBio() { return bio; }
    public String getValues() { return values; }
    public List<String> getAdditionalImageUrls() { return additionalImageUrls; }
    public List<String> getPreferences() { return preferences; }
    public String getNonNegotiable1() { return nonNegotiable1; }
    public String getNonNegotiable2() { return nonNegotiable2; }
    public String getNonNegotiable3() { return nonNegotiable3; }
    public String getDietStyle() { return dietStyle; }
    public String getHealthGoals() { return healthGoals; }
    public String getAllergiesOrRestrictions() { return allergiesOrRestrictions; }
    public String getIdealWeekendActivity() { return idealWeekendActivity; }
    public String getFictionalDinnerGuest() { return fictionalDinnerGuest; }
    public String getThreeWordsFromFriend() { return threeWordsFromFriend; }
    public String getSurprisingPassion() { return surprisingPassion; }
    public String getEmotionalIntelligence() { return emotionalIntelligence; }
    public boolean isBasicInfoComplete() { return basicInfoComplete; }
    public boolean isImagesComplete() { return imagesComplete; }
    public boolean isAboutYouComplete() { return aboutYouComplete; }
    public boolean isPreferencesComplete() { return preferencesComplete; }
    public boolean isNonNegotiablesComplete() { return nonNegotiablesComplete; }
    public boolean isNutritionVibeComplete() { return nutritionVibeComplete; }
    public boolean isPersonalityQuizComplete() { return personalityQuizComplete; }
    public boolean isIdentityVerified() { return identityVerified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public void setGenderIdentity(String genderIdentity) { this.genderIdentity = genderIdentity; }
    public void setGenderPreference(String genderPreference) { this.genderPreference = genderPreference; }
    public void setRelationshipGoal(String relationshipGoal) { this.relationshipGoal = relationshipGoal; }
    public void setReasonForJoining(String reasonForJoining) { this.reasonForJoining = reasonForJoining; }
    public void setCity(String city) { this.city = city; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }
    public void setBio(String bio) { this.bio = bio; }
    public void setValues(String values) { this.values = values; }
    public void setAdditionalImageUrls(List<String> additionalImageUrls) { this.additionalImageUrls = additionalImageUrls; }
    public void setPreferences(List<String> preferences) { this.preferences = preferences; }
    public void setNonNegotiable1(String nonNegotiable1) { this.nonNegotiable1 = nonNegotiable1; }
    public void setNonNegotiable2(String nonNegotiable2) { this.nonNegotiable2 = nonNegotiable2; }
    public void setNonNegotiable3(String nonNegotiable3) { this.nonNegotiable3 = nonNegotiable3; }
    public void setDietStyle(String dietStyle) { this.dietStyle = dietStyle; }
    public void setHealthGoals(String healthGoals) { this.healthGoals = healthGoals; }
    public void setAllergiesOrRestrictions(String allergiesOrRestrictions) { this.allergiesOrRestrictions = allergiesOrRestrictions; }
    public void setIdealWeekendActivity(String idealWeekendActivity) { this.idealWeekendActivity = idealWeekendActivity; }
    public void setFictionalDinnerGuest(String fictionalDinnerGuest) { this.fictionalDinnerGuest = fictionalDinnerGuest; }
    public void setThreeWordsFromFriend(String threeWordsFromFriend) { this.threeWordsFromFriend = threeWordsFromFriend; }
    public void setSurprisingPassion(String surprisingPassion) { this.surprisingPassion = surprisingPassion; }
    public void setEmotionalIntelligence(String emotionalIntelligence) { this.emotionalIntelligence = emotionalIntelligence; }
    public void setBasicInfoComplete(boolean basicInfoComplete) { this.basicInfoComplete = basicInfoComplete; }
    public void setImagesComplete(boolean imagesComplete) { this.imagesComplete = imagesComplete; }
    public void setAboutYouComplete(boolean aboutYouComplete) { this.aboutYouComplete = aboutYouComplete; }
    public void setPreferencesComplete(boolean preferencesComplete) { this.preferencesComplete = preferencesComplete; }
    public void setNonNegotiablesComplete(boolean nonNegotiablesComplete) { this.nonNegotiablesComplete = nonNegotiablesComplete; }
    public void setNutritionVibeComplete(boolean nutritionVibeComplete) { this.nutritionVibeComplete = nutritionVibeComplete; }
    public void setPersonalityQuizComplete(boolean personalityQuizComplete) { this.personalityQuizComplete = personalityQuizComplete; }
    public void setIdentityVerified(boolean identityVerified) { this.identityVerified = identityVerified; }
}