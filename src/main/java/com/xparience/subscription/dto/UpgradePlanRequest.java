package com.xparience.subscription.dto;

import com.xparience.subscription.BillingCycle;
import com.xparience.subscription.PaymentMethod;
import com.xparience.subscription.SubscriptionPlan;
import jakarta.validation.constraints.NotNull;

public class UpgradePlanRequest {

    @NotNull(message = "Subscription plan is required")
    private SubscriptionPlan plan;

    @NotNull(message = "Billing cycle is required")
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    private String paymentReference;
    private String paymentProvider = "STRIPE";
    private String promoCode;
    private boolean useTrial;

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public BillingCycle getBillingCycle() {
        return billingCycle;
    }

    public void setBillingCycle(BillingCycle billingCycle) {
        this.billingCycle = billingCycle;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
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

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public boolean isUseTrial() {
        return useTrial;
    }

    public void setUseTrial(boolean useTrial) {
        this.useTrial = useTrial;
    }
}
