package com.xparience.subscription;

import com.xparience.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan = SubscriptionPlan.FREE;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod = PaymentMethod.CARD;

    private BigDecimal amountPaid;
    private BigDecimal subtotalAmount;
    private BigDecimal vatAmount;
    private BigDecimal discountAmount;
    private String discountCode;
    private String currency = "GBP";
    private String paymentReference;
    private String paymentProvider;
    private String stripeCustomerId;
    private String stripeSubscriptionId;
    private String stripeInvoiceId;
    private LocalDateTime startDate;
    private LocalDateTime renewalDate;
    private LocalDateTime trialEndsAt;
    private boolean trialUsed;
    private boolean cancelAtPeriodEnd;
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan scheduledDowngradePlan;
    private LocalDateTime scheduledDowngradeAt;
    private Integer paymentFailureCount = 0;
    private LocalDateTime nextPaymentRetryAt;
    private LocalDateTime suspendedAt;
    private LocalDateTime gracePeriodEndsAt;
    private LocalDateTime reactivationEligibleUntil;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
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

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(BigDecimal vatAmount) {
        this.vatAmount = vatAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
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

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }

    public String getStripeInvoiceId() {
        return stripeInvoiceId;
    }

    public void setStripeInvoiceId(String stripeInvoiceId) {
        this.stripeInvoiceId = stripeInvoiceId;
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

    public boolean isTrialUsed() {
        return trialUsed;
    }

    public void setTrialUsed(boolean trialUsed) {
        this.trialUsed = trialUsed;
    }

    public boolean isCancelAtPeriodEnd() {
        return cancelAtPeriodEnd;
    }

    public void setCancelAtPeriodEnd(boolean cancelAtPeriodEnd) {
        this.cancelAtPeriodEnd = cancelAtPeriodEnd;
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

    public LocalDateTime getSuspendedAt() {
        return suspendedAt;
    }

    public void setSuspendedAt(LocalDateTime suspendedAt) {
        this.suspendedAt = suspendedAt;
    }

    public LocalDateTime getGracePeriodEndsAt() {
        return gracePeriodEndsAt;
    }

    public void setGracePeriodEndsAt(LocalDateTime gracePeriodEndsAt) {
        this.gracePeriodEndsAt = gracePeriodEndsAt;
    }

    public LocalDateTime getReactivationEligibleUntil() {
        return reactivationEligibleUntil;
    }

    public void setReactivationEligibleUntil(LocalDateTime reactivationEligibleUntil) {
        this.reactivationEligibleUntil = reactivationEligibleUntil;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
