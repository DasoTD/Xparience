package com.xparience.subscription;

import java.math.BigDecimal;
import java.util.List;

public enum SubscriptionPlan {

    FREE(
            "Free",
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            5,
            true,
            0,
            List.of(
                    "5 matches weekly average",
                    "Unlimited chat",
                    "AI icebreakers: max 3/day",
                    "Virtual dates: max 2 per person",
                    "Ads visible"
            )
    ),
    BASIC(
            "Basic",
            new BigDecimal("14.99"),
            new BigDecimal("35.00"),
            new BigDecimal("130.00"),
            9,
            true,
            0,
            List.of(
                    "Average 6-9 matches weekly",
                    "Unlimited chat",
                    "AI icebreakers: max 3/day",
                    "Virtual dates: max 2 per person",
                    "Ads visible"
            )
    ),
    PREMIUM(
            "Premium",
            new BigDecimal("29.99"),
            new BigDecimal("80.00"),
            new BigDecimal("310.00"),
            12,
            false,
            0,
            List.of(
                    "10+ matches weekly",
                    "Unlimited chat",
                    "AI icebreakers: max 3/day",
                    "Virtual dates: max 2 per person",
                    "No ads"
            )
    ),
    ELITE(
            "Elite",
            new BigDecimal("39.99"),
            new BigDecimal("110.00"),
            new BigDecimal("420.00"),
            12,
            false,
            1,
            List.of(
                    "10+ matches weekly",
                    "Unlimited chat",
                    "AI icebreakers: max 3/day",
                    "Virtual dates: max 2 per person",
                    "No ads",
                    "1 coaching session per month"
            )
    );

    private final String displayName;
    private final BigDecimal monthlyPrice;
    private final BigDecimal quarterlyPrice;
    private final BigDecimal yearlyPrice;
    private final int weeklyMatches;
    private final boolean adsEnabled;
    private final int coachingSessionsPerMonth;
    private final List<String> features;

    SubscriptionPlan(String displayName,
                     BigDecimal monthlyPrice,
                     BigDecimal quarterlyPrice,
                     BigDecimal yearlyPrice,
                     int weeklyMatches,
                     boolean adsEnabled,
                     int coachingSessionsPerMonth,
                     List<String> features) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.quarterlyPrice = quarterlyPrice;
        this.yearlyPrice = yearlyPrice;
        this.weeklyMatches = weeklyMatches;
        this.adsEnabled = adsEnabled;
        this.coachingSessionsPerMonth = coachingSessionsPerMonth;
        this.features = features;
    }

    public String getDisplayName() { return displayName; }
    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public BigDecimal getQuarterlyPrice() { return quarterlyPrice; }
    public BigDecimal getYearlyPrice() { return yearlyPrice; }
    public int getWeeklyMatches() { return weeklyMatches; }
    public boolean isAdsEnabled() { return adsEnabled; }
    public int getCoachingSessionsPerMonth() { return coachingSessionsPerMonth; }
    public String[] getFeatures() { return features.toArray(String[]::new); }

    public BigDecimal getPrice(BillingCycle billingCycle) {
        return switch (billingCycle) {
            case MONTHLY -> monthlyPrice;
            case QUARTERLY -> quarterlyPrice;
            case YEARLY -> yearlyPrice;
        };
    }
}