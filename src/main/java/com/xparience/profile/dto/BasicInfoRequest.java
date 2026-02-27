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
    private Double latitude;
    private Double longitude;
    private Integer matchRadiusKm;
    private String timezone;

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
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Integer getMatchRadiusKm() { return matchRadiusKm; }
    public void setMatchRadiusKm(Integer matchRadiusKm) { this.matchRadiusKm = matchRadiusKm; }
    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
}
