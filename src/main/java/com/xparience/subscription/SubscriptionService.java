package com.xparience.subscription;

import com.xparience.subscription.dto.DowngradePlanRequest;
import com.xparience.subscription.dto.CoachingMarketplaceResponse;
import com.xparience.subscription.dto.CoachingPackageDto;
import com.xparience.subscription.dto.PaymentFailureRequest;
import com.xparience.subscription.dto.PlanOptionDto;
import com.xparience.subscription.dto.ReferralSummaryResponse;
import com.xparience.subscription.dto.SubscriptionResponse;
import com.xparience.subscription.dto.UpgradePlanRequest;
import com.stripe.model.Invoice;
import com.stripe.model.checkout.Session;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final BigDecimal VAT_RATE = new BigDecimal("0.20");
    private static final BigDecimal REFERRAL_PERCENT_DISCOUNT = new BigDecimal("0.15");
    private static final List<Integer> PAYMENT_RETRY_DAYS = List.of(3, 7, 14);
    private static final String STRIPE_PROVIDER = "STRIPE";

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ReferralProgramRepository referralProgramRepository;
    private final ReferralRedemptionRepository referralRedemptionRepository;
    private final SubscriptionNotificationService subscriptionNotificationService;
    public Long getCurrentUserId() {
        return getCurrentUser().getId();
    }


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public SubscriptionResponse getCurrentSubscription() {
        User user = getCurrentUser();

        Subscription subscription = getOrCreateSubscription(user);
        applyScheduledDowngradeIfDue(subscription);
        applyCancellationIfDue(subscription);

        List<PlanOptionDto> allPlans = Arrays.stream(SubscriptionPlan.values())
                .map(plan -> {
                    PlanOptionDto option = new PlanOptionDto();
                    option.setPlan(plan);
                    option.setDisplayName(plan.getDisplayName());
                    option.setMonthlyPrice(plan.getMonthlyPrice());
                    option.setQuarterlyPrice(plan.getQuarterlyPrice());
                    option.setYearlyPrice(plan.getYearlyPrice());
                    option.setWeeklyMatches(plan.getWeeklyMatches());
                    option.setAdsEnabled(plan.isAdsEnabled());
                    option.setCoachingSessionsPerMonth(plan.getCoachingSessionsPerMonth());
                    option.setFeatures(Arrays.asList(plan.getFeatures()));
                    option.setCurrent(plan == subscription.getPlan());
                    return option;
                })
                .collect(Collectors.toList());

        SubscriptionResponse response = toResponse(subscription);
        response.setAvailablePlans(allPlans);
        return response;
    }

    @Transactional
    public SubscriptionResponse upgradePlan(UpgradePlanRequest request) {
        User user = getCurrentUser();
        Subscription subscription = getOrCreateSubscription(user);

        validatePaymentProvider(request.getPaymentProvider());

        if (request.getPlan() == SubscriptionPlan.FREE) {
            throw new RuntimeException("Use cancel endpoint to move to Free plan");
        }

        if (subscription.getPlan() == request.getPlan()
                && subscription.getBillingCycle() == request.getBillingCycle()
                && !subscription.isCancelAtPeriodEnd()) {
            throw new RuntimeException("You are already on the " +
                    request.getPlan().getDisplayName() + " plan");
        }

        if (request.isUseTrial()) {
            applyTrial(request, subscription);
            subscriptionRepository.save(subscription);
            return getCurrentSubscription();
        }

        BillingCalculation billing = calculateBilling(request.getPlan(), request.getBillingCycle(), request.getPromoCode(), user);
        BigDecimal proratedCredit = calculateProratedCredit(subscription);
        BigDecimal totalAfterCredit = billing.total().subtract(proratedCredit).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        subscription.setPlan(request.getPlan());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setPaymentMethod(request.getPaymentMethod());
        subscription.setSubtotalAmount(billing.subtotal());
        subscription.setDiscountAmount(billing.discount());
        subscription.setVatAmount(billing.vat());
        subscription.setAmountPaid(totalAfterCredit);
        subscription.setDiscountCode(normalize(request.getPromoCode()));
        subscription.setPaymentReference(request.getPaymentReference());
        subscription.setPaymentProvider(STRIPE_PROVIDER);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setRenewalDate(LocalDateTime.now().plusMonths(request.getBillingCycle().getMonths()));
        subscription.setCancelAtPeriodEnd(false);
        subscription.setCancelledAt(null);
        subscription.setScheduledDowngradePlan(null);
        subscription.setScheduledDowngradeAt(null);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setPaymentFailureCount(0);
        subscription.setNextPaymentRetryAt(null);
        subscription.setSuspendedAt(null);
        subscription.setGracePeriodEndsAt(null);
        subscription.setReactivationEligibleUntil(null);
        subscription.setTrialEndsAt(null);

        if (request.getPaymentReference() == null || request.getPaymentReference().isBlank()) {
            subscription.setPaymentReference("stripe_pay_" + System.currentTimeMillis());
        }

        subscriptionRepository.save(subscription);
        subscriptionNotificationService.sendSubscriptionConfirmation(user, subscription, "Plan upgrade");
        return getCurrentSubscription();
    }

    @Transactional
    public String cancelSubscription() {
        User user = getCurrentUser();

        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No active subscription found"));

        if (subscription.getPlan() == SubscriptionPlan.FREE) {
            throw new RuntimeException("Free plan does not require cancellation");
        }

        subscription.setCancelAtPeriodEnd(true);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setReactivationEligibleUntil(LocalDateTime.now().plusDays(30));
        subscriptionRepository.save(subscription);
        subscriptionNotificationService.sendSubscriptionConfirmation(user, subscription, "Cancellation scheduled");

        return "Subscription cancellation scheduled. Access remains until end of billing period.";
    }

    @Transactional
    public SubscriptionResponse scheduleDowngrade(DowngradePlanRequest request) {
        User user = getCurrentUser();
        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No active subscription found"));

        if (request.getTargetPlan().ordinal() >= subscription.getPlan().ordinal()) {
            throw new RuntimeException("Downgrade target must be lower than current plan");
        }

        subscription.setScheduledDowngradePlan(request.getTargetPlan());
        subscription.setScheduledDowngradeAt(subscription.getRenewalDate());
        subscriptionRepository.save(subscription);
        return getCurrentSubscription();
    }

    @Transactional
    public SubscriptionResponse reactivateSubscription() {
        User user = getCurrentUser();
        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No subscription found"));

        if (subscription.getReactivationEligibleUntil() == null
                || LocalDateTime.now().isAfter(subscription.getReactivationEligibleUntil())) {
            throw new RuntimeException("Reactivation window expired");
        }

        subscription.setCancelAtPeriodEnd(false);
        subscription.setCancelledAt(null);
        subscription.setReactivationEligibleUntil(null);
        subscriptionRepository.save(subscription);
        subscriptionNotificationService.sendSubscriptionConfirmation(user, subscription, "Subscription reactivated");
        return getCurrentSubscription();
    }

    @Transactional
    public SubscriptionResponse recordPaymentEvent(PaymentFailureRequest request) {
        User user = getCurrentUser();
        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No subscription found"));

        if (request.isPaymentRecovered()) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setPaymentFailureCount(0);
            subscription.setNextPaymentRetryAt(null);
            subscription.setSuspendedAt(null);
            subscription.setGracePeriodEndsAt(null);
            subscriptionRepository.save(subscription);
            return getCurrentSubscription();
        }

        int failures = (subscription.getPaymentFailureCount() == null ? 0 : subscription.getPaymentFailureCount()) + 1;
        subscription.setPaymentFailureCount(failures);
        subscription.setStatus(SubscriptionStatus.PAST_DUE);

        if (failures <= PAYMENT_RETRY_DAYS.size()) {
            subscription.setNextPaymentRetryAt(LocalDateTime.now().plusDays(PAYMENT_RETRY_DAYS.get(failures - 1)));
        }

        if (failures >= 3) {
            subscription.setStatus(SubscriptionStatus.SUSPENDED);
            subscription.setSuspendedAt(LocalDateTime.now());
            subscription.setGracePeriodEndsAt(LocalDateTime.now().plusDays(30));
        }

        subscriptionRepository.save(subscription);
        return getCurrentSubscription();
    }

    @Transactional
    public void processStripeCheckoutCompleted(Session session) {
        Map<String, String> metadata = session.getMetadata();
        if (metadata == null || metadata.isEmpty()) {
            return;
        }

        Long userId = Long.parseLong(metadata.get("userId"));
        SubscriptionPlan plan = SubscriptionPlan.valueOf(metadata.get("plan"));
        BillingCycle billingCycle = BillingCycle.valueOf(metadata.get("billingCycle"));
        PaymentMethod paymentMethod = PaymentMethod.valueOf(metadata.get("paymentMethod"));
        String promoCode = metadata.get("promoCode");
        boolean useTrial = Boolean.parseBoolean(metadata.getOrDefault("useTrial", "false"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found for Stripe checkout session"));
        Subscription subscription = getOrCreateSubscription(user);

        if (useTrial) {
            UpgradePlanRequest trialReq = new UpgradePlanRequest();
            trialReq.setPlan(plan);
            trialReq.setBillingCycle(billingCycle);
            trialReq.setPaymentMethod(paymentMethod);
            trialReq.setPromoCode(promoCode);
            trialReq.setUseTrial(true);
            trialReq.setPaymentProvider(STRIPE_PROVIDER);
            applyTrial(trialReq, subscription);
        } else {
            BillingCalculation billing = calculateBilling(plan, billingCycle, promoCode, user);

            subscription.setPlan(plan);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setBillingCycle(billingCycle);
            subscription.setPaymentMethod(paymentMethod);
            subscription.setSubtotalAmount(billing.subtotal());
            subscription.setDiscountAmount(billing.discount());
            subscription.setVatAmount(billing.vat());
            subscription.setAmountPaid(billing.total());
            subscription.setDiscountCode(normalize(promoCode));
            subscription.setStartDate(LocalDateTime.now());
            subscription.setRenewalDate(LocalDateTime.now().plusMonths(billingCycle.getMonths()));
            subscription.setCancelAtPeriodEnd(false);
            subscription.setCancelledAt(null);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setTrialEndsAt(null);
        }

        subscription.setPaymentProvider(STRIPE_PROVIDER);
        subscription.setPaymentReference(session.getPaymentIntent());
        subscription.setStripeCustomerId(session.getCustomer());
        subscription.setStripeSubscriptionId(session.getSubscription());
        subscription.setStripeInvoiceId(session.getInvoice());
        subscription.setPaymentFailureCount(0);
        subscription.setNextPaymentRetryAt(null);
        subscription.setSuspendedAt(null);
        subscription.setGracePeriodEndsAt(null);

        subscriptionRepository.save(subscription);
        subscriptionNotificationService.sendSubscriptionConfirmation(user, subscription, "Stripe checkout completed");
    }

    @Transactional
    public void processStripeInvoicePayment(Invoice invoice, boolean success) {
        Subscription subscription = subscriptionRepository.findByStripeCustomerId(invoice.getCustomer()).orElse(null);

        if (subscription == null) {
            return;
        }

        String invoiceId = invoice.getId();
        boolean duplicateInvoiceEvent = invoiceId != null
                && invoiceId.equals(subscription.getStripeInvoiceId())
                && (subscription.getStatus() == SubscriptionStatus.ACTIVE
                || subscription.getStatus() == SubscriptionStatus.PAST_DUE
                || subscription.getStatus() == SubscriptionStatus.SUSPENDED);

        if (duplicateInvoiceEvent) {
            return;
        }

        subscription.setStripeInvoiceId(invoice.getId());

        if (success) {
            if (subscription.getStatus() == SubscriptionStatus.CANCELLED
                    && !subscription.isCancelAtPeriodEnd()) {
                return;
            }

            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setPaymentFailureCount(0);
            subscription.setNextPaymentRetryAt(null);
            subscription.setSuspendedAt(null);
            subscription.setGracePeriodEndsAt(null);
            subscription.setRenewalDate(LocalDateTime.now().plusMonths(subscription.getBillingCycle().getMonths()));
            subscriptionRepository.save(subscription);
            return;
        }

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            return;
        }

        int failures = (subscription.getPaymentFailureCount() == null ? 0 : subscription.getPaymentFailureCount()) + 1;
        subscription.setPaymentFailureCount(Math.min(failures, 3));
        if (subscription.getStatus() != SubscriptionStatus.SUSPENDED) {
            subscription.setStatus(SubscriptionStatus.PAST_DUE);
        }

        if (failures <= PAYMENT_RETRY_DAYS.size()) {
            subscription.setNextPaymentRetryAt(LocalDateTime.now().plusDays(PAYMENT_RETRY_DAYS.get(failures - 1)));
        }

        if (failures >= 3) {
            subscription.setStatus(SubscriptionStatus.SUSPENDED);
            subscription.setSuspendedAt(LocalDateTime.now());
            subscription.setGracePeriodEndsAt(LocalDateTime.now().plusDays(30));
        }

        subscriptionRepository.save(subscription);
    }

    private Subscription getOrCreateSubscription(User user) {
        return subscriptionRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Subscription free = new Subscription();
                    free.setUser(user);
                    free.setPlan(SubscriptionPlan.FREE);
                    free.setStatus(SubscriptionStatus.ACTIVE);
                    free.setBillingCycle(BillingCycle.MONTHLY);
                    free.setStartDate(LocalDateTime.now());
                    return subscriptionRepository.save(free);
                });
    }

    public String getOrCreateReferralCode() {
        User user = getCurrentUser();
        return getOrCreateReferralProgram(user).getReferralCode();
    }

    public ReferralProgram getReferralSummary() {
        User user = getCurrentUser();
        return getOrCreateReferralProgram(user);
    }

    public ReferralSummaryResponse getReferralSummaryResponse() {
        ReferralProgram program = getReferralSummary();
        ReferralSummaryResponse response = new ReferralSummaryResponse();
        response.setReferralCode(program.getReferralCode());
        response.setSuccessfulReferrals(program.getSuccessfulReferrals() == null ? 0 : program.getSuccessfulReferrals());
        response.setTotalDiscountsGranted(valueOrZero(program.getTotalDiscountsGranted()));
        return response;
    }

    public CoachingMarketplaceResponse getCoachingMarketplace() {
        User user = getCurrentUser();
        Subscription subscription = getOrCreateSubscription(user);
        boolean accessGranted = subscription.getPlan() == SubscriptionPlan.ELITE
                && (subscription.getStatus() == SubscriptionStatus.ACTIVE || subscription.getStatus() == SubscriptionStatus.TRIAL);

        CoachingMarketplaceResponse response = new CoachingMarketplaceResponse();
        response.setAccessGranted(accessGranted);
        response.setMessage(accessGranted
                ? "Coaching marketplace unlocked for Elite tier"
                : "Upgrade to Elite to access coaching marketplace");

        CoachingPackageDto package1 = new CoachingPackageDto();
        package1.setPackageId("coach-starter-30m");
        package1.setTitle("Starter Nutrition Dating Coaching");
        package1.setDuration("30 minutes");
        package1.setPrice(new BigDecimal("49.99"));

        CoachingPackageDto package2 = new CoachingPackageDto();
        package2.setPackageId("coach-growth-60m");
        package2.setTitle("Growth Session: Profile + Match Strategy");
        package2.setDuration("60 minutes");
        package2.setPrice(new BigDecimal("89.99"));

        CoachingPackageDto package3 = new CoachingPackageDto();
        package3.setPackageId("coach-elite-90m");
        package3.setTitle("Elite Intensive: Conversion + Relationship Plan");
        package3.setDuration("90 minutes");
        package3.setPrice(new BigDecimal("129.99"));

        response.setPackages(List.of(package1, package2, package3));
        return response;
    }

    private ReferralProgram getOrCreateReferralProgram(User user) {
        return referralProgramRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ReferralProgram program = new ReferralProgram();
                    program.setUser(user);
                    program.setReferralCode(generateUniqueReferralCode(user));
                    return referralProgramRepository.save(program);
                });
    }

    private void validatePaymentProvider(String paymentProvider) {
        if (paymentProvider == null) {
            throw new RuntimeException("Payment provider is required");
        }
        if (!STRIPE_PROVIDER.equalsIgnoreCase(paymentProvider.trim())) {
            throw new RuntimeException("Only Stripe (UK) payment provider is supported");
        }
    }

    private void applyTrial(UpgradePlanRequest request, Subscription subscription) {
        if (request.getPlan() != SubscriptionPlan.PREMIUM) {
            throw new RuntimeException("Trial offer is only available for Premium plan");
        }
        if (subscription.isTrialUsed()) {
            throw new RuntimeException("Premium trial has already been used");
        }

        subscription.setPlan(SubscriptionPlan.PREMIUM);
        subscription.setBillingCycle(BillingCycle.MONTHLY);
        subscription.setPaymentMethod(request.getPaymentMethod());
        subscription.setSubtotalAmount(BigDecimal.ZERO);
        subscription.setDiscountAmount(BigDecimal.ZERO);
        subscription.setVatAmount(BigDecimal.ZERO);
        subscription.setAmountPaid(BigDecimal.ZERO);
        subscription.setStatus(SubscriptionStatus.TRIAL);
        subscription.setTrialUsed(true);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setTrialEndsAt(LocalDateTime.now().plusDays(7));
        subscription.setRenewalDate(LocalDateTime.now().plusDays(7));
        subscription.setCancelAtPeriodEnd(false);
        subscription.setDiscountCode("TRIAL7");
        subscription.setPaymentProvider(STRIPE_PROVIDER);
    }

    private BillingCalculation calculateBilling(SubscriptionPlan plan, BillingCycle billingCycle, String promoCode, User purchaser) {
        BigDecimal subtotal = plan.getPrice(billingCycle).setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = calculateDiscount(subtotal, promoCode, purchaser);
        BigDecimal taxable = subtotal.subtract(discount).max(BigDecimal.ZERO);
        BigDecimal vat = taxable.multiply(VAT_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = taxable.add(vat).setScale(2, RoundingMode.HALF_UP);
        return new BillingCalculation(subtotal, discount, vat, total);
    }

    private BigDecimal calculateDiscount(BigDecimal subtotal, String promoCode, User purchaser) {
        String code = normalize(promoCode);
        if (code == null) {
            return BigDecimal.ZERO;
        }

        if (code.startsWith("REF-")) {
            ReferralProgram referrerProgram = referralProgramRepository.findByReferralCode(code)
                    .orElseThrow(() -> new RuntimeException("Invalid referral code"));

            if (referrerProgram.getUser().getId().equals(purchaser.getId())) {
                throw new RuntimeException("You cannot use your own referral code");
            }

            if (referralRedemptionRepository.existsByRedeemedByUserId(purchaser.getId())) {
                throw new RuntimeException("Referral code already used on this account");
            }

            BigDecimal discount = subtotal.multiply(REFERRAL_PERCENT_DISCOUNT).setScale(2, RoundingMode.HALF_UP);

            ReferralRedemption redemption = new ReferralRedemption();
            redemption.setReferrerUser(referrerProgram.getUser());
            redemption.setRedeemedByUser(purchaser);
            redemption.setReferralCode(code);
            referralRedemptionRepository.save(redemption);

            int referralCount = referrerProgram.getSuccessfulReferrals() == null ? 0 : referrerProgram.getSuccessfulReferrals();
            referrerProgram.setSuccessfulReferrals(referralCount + 1);
            BigDecimal runningDiscount = referrerProgram.getTotalDiscountsGranted() == null
                    ? BigDecimal.ZERO : referrerProgram.getTotalDiscountsGranted();
            referrerProgram.setTotalDiscountsGranted(runningDiscount.add(discount));
            referralProgramRepository.save(referrerProgram);

            return discount;
        }

        Map<String, BigDecimal> fixedDiscounts = Map.of(
                "SAVE5", new BigDecimal("5.00"),
                "SAVE10", new BigDecimal("10.00")
        );
        Map<String, BigDecimal> percentDiscounts = Map.of(
                "WELCOME10", new BigDecimal("0.10"),
                "PREMIUM20", new BigDecimal("0.20"),
                "WINBACK30", new BigDecimal("0.30")
        );

        if (fixedDiscounts.containsKey(code)) {
            return fixedDiscounts.get(code).min(subtotal).setScale(2, RoundingMode.HALF_UP);
        }

        if (percentDiscounts.containsKey(code)) {
            return subtotal.multiply(percentDiscounts.get(code)).setScale(2, RoundingMode.HALF_UP);
        }

        throw new RuntimeException("Invalid or expired promo code");
    }

    private BigDecimal calculateProratedCredit(Subscription subscription) {
        if (subscription.getPlan() == SubscriptionPlan.FREE
                || subscription.getRenewalDate() == null
                || subscription.getAmountPaid() == null
                || subscription.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0
                || LocalDateTime.now().isAfter(subscription.getRenewalDate())) {
            return BigDecimal.ZERO;
        }

        long remainingSeconds = java.time.Duration.between(LocalDateTime.now(), subscription.getRenewalDate()).toSeconds();
        LocalDateTime cycleStart = subscription.getRenewalDate().minusMonths(subscription.getBillingCycle().getMonths());
        long totalSeconds = java.time.Duration.between(cycleStart, subscription.getRenewalDate()).toSeconds();

        if (remainingSeconds <= 0 || totalSeconds <= 0) {
            return BigDecimal.ZERO;
        }

        return subscription.getAmountPaid()
                .multiply(BigDecimal.valueOf(remainingSeconds))
                .divide(BigDecimal.valueOf(totalSeconds), 2, RoundingMode.HALF_UP);
    }

    private void applyScheduledDowngradeIfDue(Subscription subscription) {
        if (subscription.getScheduledDowngradePlan() == null || subscription.getScheduledDowngradeAt() == null) {
            return;
        }

        if (LocalDateTime.now().isBefore(subscription.getScheduledDowngradeAt())) {
            return;
        }

        subscription.setPlan(subscription.getScheduledDowngradePlan());
        subscription.setScheduledDowngradePlan(null);
        subscription.setScheduledDowngradeAt(null);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setRenewalDate(LocalDateTime.now().plusMonths(subscription.getBillingCycle().getMonths()));
        subscriptionRepository.save(subscription);
    }

    private void applyCancellationIfDue(Subscription subscription) {
        if (!subscription.isCancelAtPeriodEnd() || subscription.getRenewalDate() == null) {
            return;
        }

        if (LocalDateTime.now().isBefore(subscription.getRenewalDate())) {
            return;
        }

        subscription.setPlan(SubscriptionPlan.FREE);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelAtPeriodEnd(false);
        subscription.setStartDate(LocalDateTime.now());
        subscription.setRenewalDate(null);
        subscription.setSubtotalAmount(BigDecimal.ZERO);
        subscription.setDiscountAmount(BigDecimal.ZERO);
        subscription.setVatAmount(BigDecimal.ZERO);
        subscription.setAmountPaid(BigDecimal.ZERO);
        subscriptionRepository.save(subscription);
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setSubscriptionId(subscription.getId());
        response.setCurrentPlan(subscription.getPlan());
        response.setPlanDisplayName(subscription.getPlan().getDisplayName());
        response.setBillingCycle(subscription.getBillingCycle());
        response.setMonthlyPrice(subscription.getPlan().getMonthlyPrice());
        response.setSubtotalAmount(valueOrZero(subscription.getSubtotalAmount()));
        response.setDiscountAmount(valueOrZero(subscription.getDiscountAmount()));
        response.setVatAmount(valueOrZero(subscription.getVatAmount()));
        response.setTotalAmount(valueOrZero(subscription.getAmountPaid()));
        response.setCurrency(subscription.getCurrency());
        response.setStatus(subscription.getStatus());
        response.setStartDate(subscription.getStartDate());
        response.setRenewalDate(subscription.getRenewalDate());
        response.setTrialEndsAt(subscription.getTrialEndsAt());
        response.setCancelAtPeriodEnd(subscription.isCancelAtPeriodEnd());
        response.setCancelledAt(subscription.getCancelledAt());
        response.setReactivationEligibleUntil(subscription.getReactivationEligibleUntil());
        response.setScheduledDowngradePlan(subscription.getScheduledDowngradePlan());
        response.setScheduledDowngradeAt(subscription.getScheduledDowngradeAt());
        response.setPaymentFailureCount(subscription.getPaymentFailureCount());
        response.setNextPaymentRetryAt(subscription.getNextPaymentRetryAt());
        response.setGracePeriodEndsAt(subscription.getGracePeriodEndsAt());
        response.setPremiumTrialEligible(!subscription.isTrialUsed());
        return response;
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String generateUniqueReferralCode(User user) {
        String prefix = user.getEmail() == null || user.getEmail().isBlank()
                ? "USER" : user.getEmail().split("@")[0].replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        if (prefix.isBlank()) {
            prefix = "USER";
        }
        prefix = prefix.substring(0, Math.min(prefix.length(), 6));

        String code;
        do {
            code = "REF-" + prefix + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase(Locale.ROOT);
        } while (referralProgramRepository.findByReferralCode(code).isPresent());
        return code;
    }

    private record BillingCalculation(BigDecimal subtotal, BigDecimal discount, BigDecimal vat, BigDecimal total) {
    }
}