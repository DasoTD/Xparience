package com.xparience.subscription.dto;

import com.xparience.subscription.SubscriptionPlan;
import com.xparience.subscription.SubscriptionStatus;

public class SubscriptionEntitlementsResponse {
    private SubscriptionPlan currentPlan;
    private SubscriptionStatus subscriptionStatus;
    private boolean adsEnabled;
    private boolean coachingMarketplaceAccess;
    private int coachingSessionsPerMonth;
    private boolean advancedMatchingEnabled;
    private boolean unlimitedChatEnabled;

    public SubscriptionPlan getCurrentPlan() { return currentPlan; }
    public void setCurrentPlan(SubscriptionPlan currentPlan) { this.currentPlan = currentPlan; }
    public SubscriptionStatus getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }
    public boolean isAdsEnabled() { return adsEnabled; }
    public void setAdsEnabled(boolean adsEnabled) { this.adsEnabled = adsEnabled; }
    public boolean isCoachingMarketplaceAccess() { return coachingMarketplaceAccess; }
    public void setCoachingMarketplaceAccess(boolean coachingMarketplaceAccess) { this.coachingMarketplaceAccess = coachingMarketplaceAccess; }
    public int getCoachingSessionsPerMonth() { return coachingSessionsPerMonth; }
    public void setCoachingSessionsPerMonth(int coachingSessionsPerMonth) { this.coachingSessionsPerMonth = coachingSessionsPerMonth; }
    public boolean isAdvancedMatchingEnabled() { return advancedMatchingEnabled; }
    public void setAdvancedMatchingEnabled(boolean advancedMatchingEnabled) { this.advancedMatchingEnabled = advancedMatchingEnabled; }
    public boolean isUnlimitedChatEnabled() { return unlimitedChatEnabled; }
    public void setUnlimitedChatEnabled(boolean unlimitedChatEnabled) { this.unlimitedChatEnabled = unlimitedChatEnabled; }
}
