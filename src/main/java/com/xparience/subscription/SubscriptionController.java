package com.xparience.subscription;

import com.xparience.common.ApiResponse;
import com.xparience.subscription.dto.CheckoutSessionResponse;
import com.xparience.subscription.dto.CoachingMarketplaceResponse;
import com.xparience.subscription.dto.DowngradePlanRequest;
import com.xparience.subscription.dto.PaymentFailureRequest;
import com.xparience.subscription.dto.ReferralSummaryResponse;
import com.xparience.subscription.dto.StripeWebhookResponse;
import com.xparience.subscription.dto.SubscriptionEntitlementsResponse;
import com.xparience.subscription.dto.SubscriptionResponse;
import com.xparience.subscription.dto.UpgradePlanRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Tag(name = "Subscription", description = "Subscription plan management")
@SecurityRequirement(name = "bearerAuth")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
        private final StripeService stripeService;
        private final SubscriptionEntitlementService entitlementService;

    @GetMapping
    @Operation(summary = "Get current subscription and all available plans")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentSubscription() {
        return ResponseEntity.ok(ApiResponse.success(
                "Subscription details", subscriptionService.getCurrentSubscription()));
    }

    @PostMapping("/upgrade")
    @Operation(summary = "Upgrade subscription plan")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> upgradePlan(
            @Valid @RequestBody UpgradePlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Plan upgraded successfully", subscriptionService.upgradePlan(request)));
    }

    @PostMapping("/checkout-session")
    @Operation(summary = "Create Stripe checkout session for subscription")
    public ResponseEntity<ApiResponse<CheckoutSessionResponse>> createCheckoutSession(
            @Valid @RequestBody UpgradePlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Checkout session created", stripeService.createCheckoutSession(subscriptionService.getCurrentUserId(), request)));
    }

        @GetMapping("/entitlements")
        @Operation(summary = "Get feature-gating entitlements for current subscription tier")
        public ResponseEntity<ApiResponse<SubscriptionEntitlementsResponse>> getEntitlements() {
                return ResponseEntity.ok(ApiResponse.success(
                                "Subscription entitlements", entitlementService.getCurrentEntitlements()));
        }

        @GetMapping("/coaching-marketplace")
        @Operation(summary = "Get coaching marketplace access and package catalog")
        public ResponseEntity<ApiResponse<CoachingMarketplaceResponse>> getCoachingMarketplace() {
                return ResponseEntity.ok(ApiResponse.success(
                                "Coaching marketplace", subscriptionService.getCoachingMarketplace()));
        }

        @GetMapping("/referral")
        @Operation(summary = "Get or create referral code and referral summary")
        public ResponseEntity<ApiResponse<ReferralSummaryResponse>> getReferralSummary() {
                return ResponseEntity.ok(ApiResponse.success(
                                "Referral summary", subscriptionService.getReferralSummaryResponse()));
        }

    @PostMapping("/cancel")
    @Operation(summary = "Cancel current subscription")
    public ResponseEntity<ApiResponse<String>> cancelSubscription() {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.cancelSubscription()));
    }

    @PostMapping("/downgrade")
    @Operation(summary = "Schedule downgrade for next billing cycle")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> downgradePlan(
            @Valid @RequestBody DowngradePlanRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Downgrade scheduled", subscriptionService.scheduleDowngrade(request)));
    }

    @PostMapping("/reactivate")
    @Operation(summary = "Reactivate cancelled subscription within 30 days")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> reactivateSubscription() {
        return ResponseEntity.ok(ApiResponse.success(
                "Subscription reactivated", subscriptionService.reactivateSubscription()));
    }

    @PostMapping("/payment-event")
    @Operation(summary = "Record payment recovery/failure and update retries or suspension state")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> paymentEvent(
            @RequestBody PaymentFailureRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Payment event recorded", subscriptionService.recordPaymentEvent(request)));
    }

    @PostMapping("/stripe/webhook")
    @Operation(summary = "Stripe webhook endpoint")
    public ResponseEntity<ApiResponse<StripeWebhookResponse>> stripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        return ResponseEntity.ok(ApiResponse.success(
                "Stripe webhook processed", stripeService.handleWebhook(payload, signature)));
    }
}