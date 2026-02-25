package com.xparience.profile.dto;

public class NutritionVibeRequest {
    private String dietStyle;
    private String healthGoals;
    private String allergiesOrRestrictions;

    public String getDietStyle() { return dietStyle; }
    public void setDietStyle(String dietStyle) { this.dietStyle = dietStyle; }
    public String getHealthGoals() { return healthGoals; }
    public void setHealthGoals(String healthGoals) { this.healthGoals = healthGoals; }
    public String getAllergiesOrRestrictions() { return allergiesOrRestrictions; }
    public void setAllergiesOrRestrictions(String allergiesOrRestrictions) { this.allergiesOrRestrictions = allergiesOrRestrictions; }
}
