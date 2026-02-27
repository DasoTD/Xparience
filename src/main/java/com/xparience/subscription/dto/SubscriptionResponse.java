package com.xparience.subscription.dto;

import com.xparience.subscription.BillingCycle;
import com.xparience.subscription.SubscriptionPlan;
import com.xparience.subscription.SubscriptionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class SubscriptionResponse {
    private Long subscriptionId;
    private SubscriptionPlan currentPlan;
    private String planDisplayName;
    private BillingCycle billingCycle;
    private BigDecimal monthlyPrice;
    private BigDecimal subtotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal vatAmount;
    private BigDecimal totalAmount;
    private String currency;
    private SubscriptionStatus status;
    private LocalDateTime startDate;
    private LocalDateTime renewalDate;
    private LocalDateTime trialEndsAt;
    private boolean cancelAtPeriodEnd;
    private LocalDateTime cancelledAt;
    private LocalDateTime reactivationEligibleUntil;
    private SubscriptionPlan scheduledDowngradePlan;
    private LocalDateTime scheduledDowngradeAt;
    private Integer paymentFailureCount;
    private LocalDateTime nextPaymentRetryAt;
    private LocalDateTime gracePeriodEndsAt;
    private boolean premiumTrialEligible;
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

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public LocalDateTime getTrialEndsAt() {
        return trialEndsAt;
    }

    public void setTrialEndsAt(LocalDateTime trialEndsAt) {
        this.trialEndsAt = trialEndsAt;
    }

    public boolean isCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getReactivationEligibleUntil() {
        return reactivationEligibleUntil;
    }

    public void setReactivationEligibleUntil(LocalDateTime reactivationEligibleUntil) {
        this.reactivationEligibleUntil = reactivationEligibleUntil;
    }

    public SubscriptionPlan getScheduledDowngradePlan() {
        return scheduledDowngradePlan;
    }

    public void setScheduledDowngradePlan(SubscriptionPlan scheduledDowngradePlan) {
        this.scheduledDowngradePlan = scheduledDowngradePlan;
    }

    public LocalDateTime getScheduledDowngradeAt() {
        return scheduledDowngradeAt;
    }

    public void setScheduledDowngradeAt(LocalDateTime scheduledDowngradeAt) {
        this.scheduledDowngradeAt = scheduledDowngradeAt;
    }

    public Integer getPaymentFailureCount() {
        return paymentFailureCount;
    }

    public void setPaymentFailureCount(Integer paymentFailureCount) {
        this.paymentFailureCount = paymentFailureCount;
    }

    public LocalDateTime getNextPaymentRetryAt() {
        return nextPaymentRetryAt;
    }

    public void setNextPaymentRetryAt(LocalDateTime nextPaymentRetryAt) {
        this.nextPaymentRetryAt = nextPaymentRetryAt;
    }

    public LocalDateTime getGracePeriodEndsAt() {
        return gracePeriodEndsAt;
    }

    public void setGracePeriodEndsAt(LocalDateTime gracePeriodEndsAt) {
        this.gracePeriodEndsAt = gracePeriodEndsAt;
    }

    public boolean isPremiumTrialEligible() {
        return premiumTrialEligible;
    }

    public void setPremiumTrialEligible(boolean premiumTrialEligible) {
        this.premiumTrialEligible = premiumTrialEligible;
    }

    public List<PlanOptionDto> getAvailablePlans() {
        return availablePlans;
    }

    public void setAvailablePlans(List<PlanOptionDto> availablePlans) {
        this.availablePlans = availablePlans;
    }
}