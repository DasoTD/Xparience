package com.xparience.subscription;

public enum BillingCycle {
    MONTHLY(1),
    QUARTERLY(3),
    YEARLY(12);

    private final int months;

    BillingCycle(int months) {
        this.months = months;
    }

    public int getMonths() {
        return months;
    }
}
