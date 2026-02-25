package com.xparience.chat.conversation;

import com.xparience.user.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations")
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_one_id", nullable = false)
    private User participantOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participant_two_id", nullable = false)
    private User participantTwo;

    @Column(columnDefinition = "TEXT")
    private String lastMessage;

    private LocalDateTime lastMessageAt;

    private boolean blockedByOne = false;
    private boolean blockedByTwo = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getParticipantOne() {
        return participantOne;
    }

    public void setParticipantOne(User participantOne) {
        this.participantOne = participantOne;
    }

    public User getParticipantTwo() {
        return participantTwo;
    }

    public void setParticipantTwo(User participantTwo) {
        this.participantTwo = participantTwo;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(LocalDateTime lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public boolean isBlockedByOne() {
        return blockedByOne;
    }

    public void setBlockedByOne(boolean blockedByOne) {
        this.blockedByOne = blockedByOne;
    }

    public boolean isBlockedByTwo() {
        return blockedByTwo;
    }

    public void setBlockedByTwo(boolean blockedByTwo) {
        this.blockedByTwo = blockedByTwo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
