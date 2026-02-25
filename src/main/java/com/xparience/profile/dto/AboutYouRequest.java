package com.xparience.profile.dto;

import jakarta.validation.constraints.NotBlank;

public class AboutYouRequest {
    @NotBlank
    private String bio;
    @NotBlank
    private String values;

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getValues() { return values; }
    public void setValues(String values) { this.values = values; }
}
