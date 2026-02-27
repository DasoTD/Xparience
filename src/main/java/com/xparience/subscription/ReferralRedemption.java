package com.xparience.subscription;

import com.xparience.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "referral_redemptions", uniqueConstraints = {
    @UniqueConstraint(name = "uk_referral_redeemed_user", columnNames = "redeemed_by_user_id")
})
public class ReferralRedemption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referrer_user_id", nullable = false)
    private User referrerUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redeemed_by_user_id", nullable = false)
    private User redeemedByUser;

    @Column(nullable = false, length = 32)
    private String referralCode;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getReferrerUser() { return referrerUser; }
    public void setReferrerUser(User referrerUser) { this.referrerUser = referrerUser; }
    public User getRedeemedByUser() { return redeemedByUser; }
    public void setRedeemedByUser(User redeemedByUser) { this.redeemedByUser = redeemedByUser; }
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
