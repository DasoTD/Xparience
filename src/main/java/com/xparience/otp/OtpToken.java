package com.xparience.otp;

import com.xparience.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_tokens")
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String token;

    @Enumerated(EnumType.STRING)
    private OtpType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime expiresAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private Integer attemptsRemaining;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (attemptsRemaining == null) {
            attemptsRemaining = 3;
        }
    }

    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }

    // Getters
    public Long getId() { return id; }
    public String getToken() { return token; }
    public OtpType getType() { return type; }
    public User getUser() { return user; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getAttemptsRemaining() { return attemptsRemaining; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setToken(String token) { this.token = token; }
    public void setType(OtpType type) { this.type = type; }
    public void setUser(User user) { this.user = user; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public void setAttemptsRemaining(Integer attemptsRemaining) { this.attemptsRemaining = attemptsRemaining; }
}