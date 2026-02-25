package com.xparience.verification.dto;

import com.xparience.verification.VerificationStatus;

import java.time.LocalDateTime;

public class VerificationStatusResponse {
    private Long verificationId;
    private boolean idVerificationComplete;
    private boolean faceVerificationComplete;
    private boolean faceMatchPassed;
    private Double faceMatchConfidence;
    private VerificationStatus status;
    private String reviewNote;
    private LocalDateTime submittedAt;

    public Long getVerificationId() { return verificationId; }
    public void setVerificationId(Long verificationId) { this.verificationId = verificationId; }
    public boolean isIdVerificationComplete() { return idVerificationComplete; }
    public void setIdVerificationComplete(boolean idVerificationComplete) { this.idVerificationComplete = idVerificationComplete; }
    public boolean isFaceVerificationComplete() { return faceVerificationComplete; }
    public void setFaceVerificationComplete(boolean faceVerificationComplete) { this.faceVerificationComplete = faceVerificationComplete; }
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
}