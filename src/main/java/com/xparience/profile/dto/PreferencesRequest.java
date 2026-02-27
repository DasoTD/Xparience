package com.xparience.profile.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class PreferencesRequest {
    @NotEmpty
    @Size(min = 5, message = "Select at least 5 interests")
    private List<String> preferences;

    public List<String> getPreferences() { return preferences; }
    public void setPreferences(List<String> preferences) { this.preferences = preferences; }
}
