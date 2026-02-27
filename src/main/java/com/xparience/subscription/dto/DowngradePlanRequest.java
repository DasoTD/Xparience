package com.xparience.subscription.dto;

import com.xparience.subscription.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

public class DowngradePlanRequest {

    @NotNull(message = "Target plan is required")
    private SubscriptionPlan targetPlan;

    public SubscriptionPlan getTargetPlan() {
        return targetPlan;
    }

    public void setTargetPlan(SubscriptionPlan targetPlan) {
        this.targetPlan = targetPlan;
    }
}
