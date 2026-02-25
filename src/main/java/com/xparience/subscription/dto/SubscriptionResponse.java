package com.xparience.subscription.dto;

import com.xparience.subscription.SubscriptionPlan;
import com.xparience.subscription.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SubscriptionResponse {
    private Long subscriptionId;
    private SubscriptionPlan currentPlan;
    private String planDisplayName;
    private BigDecimal monthlyPrice;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime renewalDate;
    private List<PlanOptionDto> availablePlans;

    public Long getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public SubscriptionPlan getCurrentPlan() {
        return currentPlan;
    }

    public void setCurrentPlan(SubscriptionPlan currentPlan) {
        this.currentPlan = currentPlan;
    }

    public String getPlanDisplayName() {
        return planDisplayName;
    }

    public void setPlanDisplayName(String planDisplayName) {
        this.planDisplayName = planDisplayName;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getRenewalDate() {
        return renewalDate;
    }

    public void setRenewalDate(LocalDateTime renewalDate) {
        this.renewalDate = renewalDate;
    }

    public List<PlanOptionDto> getAvailablePlans() {
        return availablePlans;
    }

    public void setAvailablePlans(List<PlanOptionDto> availablePlans) {
        this.availablePlans = availablePlans;
    }
}