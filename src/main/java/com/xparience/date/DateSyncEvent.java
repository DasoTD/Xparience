package com.xparience.date;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "date_sync_events")
public class DateSyncEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_id", nullable = false)
    private DateInvite invite;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SyncAction action;

    private Long positionSeconds;

    private LocalDateTime occurredAt;

    @PrePersist
    protected void onCreate() {
        occurredAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DateInvite getInvite() {
        return invite;
    }

    public void setInvite(DateInvite invite) {
        this.invite = invite;
    }

    public Long getActorUserId() {
        return actorUserId;
    }

    public void setActorUserId(Long actorUserId) {
        this.actorUserId = actorUserId;
    }

    public SyncAction getAction() {
        return action;
    }

    public void setAction(SyncAction action) {
        this.action = action;
    }

    public Long getPositionSeconds() {
        return positionSeconds;
    }

    public void setPositionSeconds(Long positionSeconds) {
        this.positionSeconds = positionSeconds;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public void setOccurredAt(LocalDateTime occurredAt) {
        this.occurredAt = occurredAt;
    }
}
