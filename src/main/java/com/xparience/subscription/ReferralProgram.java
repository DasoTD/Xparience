package com.xparience.subscription;

import com.xparience.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "referral_programs")
public class ReferralProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 32)
    private String referralCode;

    private Integer successfulReferrals = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalDiscountsGranted = BigDecimal.ZERO;

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getReferralCode() { return referralCode; }
    public void setReferralCode(String referralCode) { this.referralCode = referralCode; }
    public Integer getSuccessfulReferrals() { return successfulReferrals; }
    public void setSuccessfulReferrals(Integer successfulReferrals) { this.successfulReferrals = successfulReferrals; }
    public BigDecimal getTotalDiscountsGranted() { return totalDiscountsGranted; }
    public void setTotalDiscountsGranted(BigDecimal totalDiscountsGranted) { this.totalDiscountsGranted = totalDiscountsGranted; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
