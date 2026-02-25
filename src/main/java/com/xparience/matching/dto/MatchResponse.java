package com.xparience.matching.dto;

import com.xparience.matching.MatchStatus;

import java.util.List;

public class MatchResponse {
    private Long matchId;
    private Long matchedUserId;
    private String fullName;
    private int age;
    private String profilePictureUrl;
    private String city;
    private String bio;
    private MatchStatus status;
    private Double overallCompatibilityScore;
    private Double profileMatchScore;
    private Double nutritionalValueScore;
    private List<String> sharedInterests;
    private String dietStyle;
    private boolean aiGenerated;

    public Long getMatchId() {
        return matchId;
    }

    public void setMatchId(Long matchId) {
        this.matchId = matchId;
    }

    public Long getMatchedUserId() {
        return matchedUserId;
    }

    public void setMatchedUserId(Long matchedUserId) {
        this.matchedUserId = matchedUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public Double getOverallCompatibilityScore() {
        return overallCompatibilityScore;
    }

    public void setOverallCompatibilityScore(Double overallCompatibilityScore) {
        this.overallCompatibilityScore = overallCompatibilityScore;
    }

    public Double getProfileMatchScore() {
        return profileMatchScore;
    }

    public void setProfileMatchScore(Double profileMatchScore) {
        this.profileMatchScore = profileMatchScore;
    }

    public Double getNutritionalValueScore() {
        return nutritionalValueScore;
    }

    public void setNutritionalValueScore(Double nutritionalValueScore) {
        this.nutritionalValueScore = nutritionalValueScore;
    }

    public List<String> getSharedInterests() {
        return sharedInterests;
    }

    public void setSharedInterests(List<String> sharedInterests) {
        this.sharedInterests = sharedInterests;
    }

    public String getDietStyle() {
        return dietStyle;
    }

    public void setDietStyle(String dietStyle) {
        this.dietStyle = dietStyle;
    }

    public boolean isAiGenerated() {
        return aiGenerated;
    }

    public void setAiGenerated(boolean aiGenerated) {
        this.aiGenerated = aiGenerated;
    }
}
