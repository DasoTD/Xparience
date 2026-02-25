package com.xparience.subscription.dto;

import com.xparience.subscription.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

public class UpgradePlanRequest {

    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan plan;

    private String paymentReference;
    private String paymentProvider;

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public String getPaymentReference() {
        return paymentReference;
    }

    public void setPaymentReference(String paymentReference) {
        this.paymentReference = paymentReference;
    }

    public String getPaymentProvider() {
        return paymentProvider;
    }

    public void setPaymentProvider(String paymentProvider) {
        this.paymentProvider = paymentProvider;
    }
}
