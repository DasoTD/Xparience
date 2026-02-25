package com.xparience.verification;

import com.xparience.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_records")
public class VerificationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Government ID
    @Enumerated(EnumType.STRING)
    private IdType idType;

    private String idNumber;
    private String idDocumentUrl;

    // Face verification
    private String selfieImageUrl;
    private boolean faceMatchPassed;
    private Double faceMatchConfidence;

    @Enumerated(EnumType.STRING)
    private VerificationStatus status = VerificationStatus.PENDING;

    private String reviewNote;

    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
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

    public VerificationRecord() {
    }

    public VerificationRecord(Long id, User user, IdType idType, String idNumber, String idDocumentUrl,
                              String selfieImageUrl, boolean faceMatchPassed, Double faceMatchConfidence,
                              VerificationStatus status, String reviewNote, LocalDateTime submittedAt,
                              LocalDateTime reviewedAt, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.idType = idType;
        this.idNumber = idNumber;
        this.idDocumentUrl = idDocumentUrl;
        this.selfieImageUrl = selfieImageUrl;
        this.faceMatchPassed = faceMatchPassed;
        this.faceMatchConfidence = faceMatchConfidence;
        this.status = status;
        this.reviewNote = reviewNote;
        this.submittedAt = submittedAt;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public IdType getIdType() { return idType; }
    public void setIdType(IdType idType) { this.idType = idType; }
    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }
    public String getIdDocumentUrl() { return idDocumentUrl; }
    public void setIdDocumentUrl(String idDocumentUrl) { this.idDocumentUrl = idDocumentUrl; }
    public String getSelfieImageUrl() { return selfieImageUrl; }
    public void setSelfieImageUrl(String selfieImageUrl) { this.selfieImageUrl = selfieImageUrl; }
    public boolean isFaceMatchPassed() { return faceMatchPassed; }
    public void setFaceMatchPassed(boolean faceMatchPassed) { this.faceMatchPassed = faceMatchPassed; }
    public Double getFaceMatchConfidence() { return faceMatchConfidence; }
    public void setFaceMatchConfidence(Double faceMatchConfidence) { this.faceMatchConfidence = faceMatchConfidence; }
    public VerificationStatus getStatus() { return status; }
    public void setStatus(VerificationStatus status) { this.status = status; }
    public String getReviewNote() { return reviewNote; }
    public void setReviewNote(String reviewNote) { this.reviewNote = reviewNote; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
    public LocalDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(LocalDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}