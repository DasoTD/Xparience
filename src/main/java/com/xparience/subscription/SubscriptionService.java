package com.xparience.subscription;

import com.xparience.subscription.dto.PlanOptionDto;
import com.xparience.subscription.dto.SubscriptionResponse;
import com.xparience.subscription.dto.UpgradePlanRequest;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public SubscriptionResponse getCurrentSubscription() {
        User user = getCurrentUser();

        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    // Auto-create FREE subscription for new users
                    Subscription free = new Subscription();
                    free.setUser(user);
                    free.setPlan(SubscriptionPlan.FREE);
                    free.setStatus(SubscriptionStatus.ACTIVE);
                    free.setStartDate(LocalDateTime.now());
                    return subscriptionRepository.save(free);
                });

        List<PlanOptionDto> allPlans = Arrays.stream(SubscriptionPlan.values())
                .map(plan -> {
                    PlanOptionDto option = new PlanOptionDto();
                    option.setPlan(plan);
                    option.setDisplayName(plan.getDisplayName());
                    option.setMonthlyPrice(plan.getMonthlyPrice());
                    option.setFeatures(Arrays.asList(plan.getFeatures()));
                    option.setCurrent(plan == subscription.getPlan());
                    return option;
                })
                .collect(Collectors.toList());

        SubscriptionResponse response = new SubscriptionResponse();
        response.setSubscriptionId(subscription.getId());
        response.setCurrentPlan(subscription.getPlan());
        response.setPlanDisplayName(subscription.getPlan().getDisplayName());
        response.setMonthlyPrice(subscription.getPlan().getMonthlyPrice());
        response.setStatus(subscription.getStatus());
        response.setStartDate(subscription.getStartDate());
        response.setRenewalDate(subscription.getRenewalDate());
        response.setAvailablePlans(allPlans);
        return response;
    }

    @Transactional
    public SubscriptionResponse upgradePlan(UpgradePlanRequest request) {
        User user = getCurrentUser();

        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                                .orElseGet(() -> {
                                        Subscription newSubscription = new Subscription();
                                        newSubscription.setUser(user);
                                        return newSubscription;
                                });

        if (subscription.getPlan() == request.getPlan()) {
            throw new RuntimeException("You are already on the " +
                    request.getPlan().getDisplayName() + " plan");
        }

        subscription.setPlan(request.getPlan());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setAmountPaid(request.getPlan().getMonthlyPrice());
        subscription.setPaymentReference(request.getPaymentReference());
        subscription.setPaymentProvider(request.getPaymentProvider());
        subscription.setStartDate(LocalDateTime.now());
        subscription.setRenewalDate(LocalDateTime.now().plusMonths(1));

        subscriptionRepository.save(subscription);
        return getCurrentSubscription();
    }

    @Transactional
    public String cancelSubscription() {
        User user = getCurrentUser();

        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("No active subscription found"));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setPlan(SubscriptionPlan.FREE);
        subscriptionRepository.save(subscription);

        return "Subscription cancelled. You have been moved to the Free plan.";
    }
}