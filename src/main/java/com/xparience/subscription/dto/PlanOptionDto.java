package com.xparience.subscription.dto;

import com.xparience.subscription.SubscriptionPlan;

import java.math.BigDecimal;
import java.util.List;

public class PlanOptionDto {
    private SubscriptionPlan plan;
    private String displayName;
    private BigDecimal monthlyPrice;
    private BigDecimal quarterlyPrice;
    private BigDecimal yearlyPrice;
    private int weeklyMatches;
    private boolean adsEnabled;
    private int coachingSessionsPerMonth;
    private List<String> features;
    private boolean isCurrent;

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public BigDecimal getQuarterlyPrice() {
        return quarterlyPrice;
    }

    public void setQuarterlyPrice(BigDecimal quarterlyPrice) {
        this.quarterlyPrice = quarterlyPrice;
    }

    public BigDecimal getYearlyPrice() {
        return yearlyPrice;
    }

    public void setYearlyPrice(BigDecimal yearlyPrice) {
        this.yearlyPrice = yearlyPrice;
    }

    public int getWeeklyMatches() {
        return weeklyMatches;
    }

    public void setWeeklyMatches(int weeklyMatches) {
        this.weeklyMatches = weeklyMatches;
    }

    public boolean isAdsEnabled() {
        return adsEnabled;
    }

    public void setAdsEnabled(boolean adsEnabled) {
        this.adsEnabled = adsEnabled;
    }

    public int getCoachingSessionsPerMonth() {
        return coachingSessionsPerMonth;
    }

    public void setCoachingSessionsPerMonth(int coachingSessionsPerMonth) {
        this.coachingSessionsPerMonth = coachingSessionsPerMonth;
    }

    public List<String> getFeatures() {
        return features;
    }

    public void setFeatures(List<String> features) {
        this.features = features;
    }

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }
}
