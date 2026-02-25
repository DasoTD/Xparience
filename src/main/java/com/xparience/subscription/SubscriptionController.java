package com.xparience.subscription;

import com.xparience.common.ApiResponse;
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

    @PostMapping("/cancel")
    @Operation(summary = "Cancel current subscription")
    public ResponseEntity<ApiResponse<String>> cancelSubscription() {
        return ResponseEntity.ok(ApiResponse.success(
                subscriptionService.cancelSubscription()));
    }
}