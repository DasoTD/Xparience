package com.xparience.date;

import com.xparience.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "date_invites")
public class DateInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Enumerated(EnumType.STRING)
    private DateType dateType;

    @Enumerated(EnumType.STRING)
    private StreamingPlatform streamingPlatform;

    private String title;
    private String description;
    private String contentLink;

    private LocalDateTime scheduledAt;

    @Enumerated(EnumType.STRING)
    private DateStatus status = DateStatus.PENDING;

    private LocalDateTime respondedAt;
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

    public DateInvite() {
    }

    public DateInvite(Long id, User sender, User recipient, DateType dateType, StreamingPlatform streamingPlatform,
                      String title, String description, String contentLink, LocalDateTime scheduledAt,
                      DateStatus status, LocalDateTime respondedAt, LocalDateTime createdAt,
                      LocalDateTime updatedAt) {
        this.id = id;
        this.sender = sender;
        this.recipient = recipient;
        this.dateType = dateType;
        this.streamingPlatform = streamingPlatform;
        this.title = title;
        this.description = description;
        this.contentLink = contentLink;
        this.scheduledAt = scheduledAt;
        this.status = status;
        this.respondedAt = respondedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }
    public DateType getDateType() { return dateType; }
    public void setDateType(DateType dateType) { this.dateType = dateType; }
    public StreamingPlatform getStreamingPlatform() { return streamingPlatform; }
    public void setStreamingPlatform(StreamingPlatform streamingPlatform) { this.streamingPlatform = streamingPlatform; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getContentLink() { return contentLink; }
    public void setContentLink(String contentLink) { this.contentLink = contentLink; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public DateStatus getStatus() { return status; }
    public void setStatus(DateStatus status) { this.status = status; }
    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) { this.respondedAt = respondedAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}