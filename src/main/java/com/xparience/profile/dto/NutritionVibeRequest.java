package com.xparience.profile.dto;

import jakarta.validation.constraints.NotBlank;

public class NutritionVibeRequest {
    @NotBlank(message = "Diet style is required")
    private String dietStyle;
    @NotBlank(message = "Cooking frequency is required")
    private String cookingFrequency;
    private String healthGoals;
    private String allergiesOrRestrictions;

    public String getDietStyle() { return dietStyle; }
    public void setDietStyle(String dietStyle) { this.dietStyle = dietStyle; }
    public String getCookingFrequency() { return cookingFrequency; }
    public void setCookingFrequency(String cookingFrequency) { this.cookingFrequency = cookingFrequency; }
    public String getHealthGoals() { return healthGoals; }
    public void setHealthGoals(String healthGoals) { this.healthGoals = healthGoals; }
    public String getAllergiesOrRestrictions() { return allergiesOrRestrictions; }
    public void setAllergiesOrRestrictions(String allergiesOrRestrictions) { this.allergiesOrRestrictions = allergiesOrRestrictions; }
}
