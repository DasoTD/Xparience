package com.xparience.subscription;

import com.xparience.subscription.dto.SubscriptionEntitlementsResponse;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionEntitlementService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionEntitlementsResponse getCurrentEntitlements() {
        User user = getCurrentUser();
        Subscription subscription = subscriptionRepository.findByUserId(user.getId()).orElseGet(() -> {
            Subscription free = new Subscription();
            free.setPlan(SubscriptionPlan.FREE);
            free.setStatus(SubscriptionStatus.ACTIVE);
            free.setBillingCycle(BillingCycle.MONTHLY);
            return free;
        });

        SubscriptionPlan plan = subscription.getPlan();
        boolean coachingMarketplaceAccess = plan == SubscriptionPlan.ELITE;

        SubscriptionEntitlementsResponse response = new SubscriptionEntitlementsResponse();
        response.setCurrentPlan(plan);
        response.setSubscriptionStatus(subscription.getStatus());
        response.setAdsEnabled(plan.isAdsEnabled());
        response.setCoachingMarketplaceAccess(coachingMarketplaceAccess);
        response.setCoachingSessionsPerMonth(plan.getCoachingSessionsPerMonth());
        response.setAdvancedMatchingEnabled(plan != SubscriptionPlan.FREE);
        response.setUnlimitedChatEnabled(true);
        return response;
    }

    public boolean hasFeature(SubscriptionFeature feature) {
        SubscriptionEntitlementsResponse entitlements = getCurrentEntitlements();
        return switch (feature) {
            case NO_ADS -> !entitlements.isAdsEnabled();
            case COACHING_MARKETPLACE -> entitlements.isCoachingMarketplaceAccess();
            case COACHING_SESSION_ACCESS -> entitlements.getCoachingSessionsPerMonth() > 0;
            case ADVANCED_MATCHING -> entitlements.isAdvancedMatchingEnabled();
            case UNLIMITED_CHAT -> entitlements.isUnlimitedChatEnabled();
        };
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
