package com.xparience.subscription;

import java.math.BigDecimal;

public enum SubscriptionPlan {

    FREE("Free", BigDecimal.ZERO, new String[]{
            "Basic matching",
            "Limited daily matches",
            "Basic chat"
    }),
    BASIC("Basic", new BigDecimal("12.00"), new String[]{
            "More features added",
            "Additional Features"
    }),
    PREMIUM("Premium", new BigDecimal("15.00"), new String[]{
            "More features added",
            "Additional features",
            "Additional level features"
    }),
    ELITE("Elite", new BigDecimal("20.00"), new String[]{
            "More features added",
            "Additional Features AI",
            "Additional level Features"
    });

    private final String displayName;
    private final BigDecimal monthlyPrice;
    private final String[] features;

    SubscriptionPlan(String displayName, BigDecimal monthlyPrice, String[] features) {
        this.displayName = displayName;
        this.monthlyPrice = monthlyPrice;
        this.features = features;
    }

    public String getDisplayName() { return displayName; }
    public BigDecimal getMonthlyPrice() { return monthlyPrice; }
    public String[] getFeatures() { return features; }
}