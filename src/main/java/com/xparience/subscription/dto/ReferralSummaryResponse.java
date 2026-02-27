package com.xparience.subscription.dto;

import java.math.BigDecimal;

public class ReferralSummaryResponse {
    private String referralCode;
    private int successfulReferrals;
    private BigDecimal totalDiscountsGranted;

    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public int getSuccessfulReferrals() { return successfulReferrals; }
    public void setSuccessfulReferrals(int successfulReferrals) { this.successfulReferrals = successfulReferrals; }
    public BigDecimal getTotalDiscountsGranted() { return totalDiscountsGranted; }
    public void setTotalDiscountsGranted(BigDecimal totalDiscountsGranted) { this.totalDiscountsGranted = totalDiscountsGranted; }
}
