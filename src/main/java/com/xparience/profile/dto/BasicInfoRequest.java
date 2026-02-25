package com.xparience.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class BasicInfoRequest {
    @NotBlank
    private String fullName;
    @NotNull
    private LocalDate dateOfBirth;
    @NotBlank
    private String genderIdentity;
    @NotBlank
    private String genderPreference;
    @NotBlank
    private String relationshipGoal;
    private String reasonForJoining;
    @NotBlank
    private String city;

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
}
