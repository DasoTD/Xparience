package com.xparience.matching;

import com.xparience.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one_id")
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two_id")
    private User userTwo;

    @Enumerated(EnumType.STRING)
    private MatchStatus status = MatchStatus.PENDING;

    // AI scoring
    private Double overallCompatibilityScore;
    private Double profileMatchScore;
    private Double nutritionalValueScore;
    private String sharedInterests;

    private boolean aiGenerated;
    private LocalDateTime matchedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Match() {
    }

    public Match(Long id, User userOne, User userTwo, MatchStatus status, Double overallCompatibilityScore,
                 Double profileMatchScore, Double nutritionalValueScore, String sharedInterests,
                 boolean aiGenerated, LocalDateTime matchedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userOne = userOne;
        this.userTwo = userTwo;
        this.status = status;
        this.overallCompatibilityScore = overallCompatibilityScore;
        this.profileMatchScore = profileMatchScore;
        this.nutritionalValueScore = nutritionalValueScore;
        this.sharedInterests = sharedInterests;
        this.aiGenerated = aiGenerated;
        this.matchedAt = matchedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUserOne() { return userOne; }
    public void setUserOne(User userOne) { this.userOne = userOne; }
    public User getUserTwo() { return userTwo; }
    public void setUserTwo(User userTwo) { this.userTwo = userTwo; }
    public MatchStatus getStatus() { return status; }
    public void setStatus(MatchStatus status) { this.status = status; }
    public Double getOverallCompatibilityScore() { return overallCompatibilityScore; }
    public void setOverallCompatibilityScore(Double overallCompatibilityScore) { this.overallCompatibilityScore = overallCompatibilityScore; }
    public Double getProfileMatchScore() { return profileMatchScore; }
    public void setProfileMatchScore(Double profileMatchScore) { this.profileMatchScore = profileMatchScore; }
    public Double getNutritionalValueScore() { return nutritionalValueScore; }
    public void setNutritionalValueScore(Double nutritionalValueScore) { this.nutritionalValueScore = nutritionalValueScore; }
    public String getSharedInterests() { return sharedInterests; }
    public void setSharedInterests(String sharedInterests) { this.sharedInterests = sharedInterests; }
    public boolean isAiGenerated() { return aiGenerated; }
    public void setAiGenerated(boolean aiGenerated) { this.aiGenerated = aiGenerated; }
    public LocalDateTime getMatchedAt() { return matchedAt; }
    public void setMatchedAt(LocalDateTime matchedAt) { this.matchedAt = matchedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}