package com.xparience.profile.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AboutYouRequest {
    @NotBlank
    @Size(min = 50, max = 500, message = "Bio must be between 50 and 500 characters")
    private String bio;
    @NotBlank
    private String values;

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getValues() { return values; }
    public void setValues(String values) { this.values = values; }
}
