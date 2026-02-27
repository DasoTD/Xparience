package com.xparience.date;

import com.xparience.chat.conversation.Conversation;
import com.xparience.chat.conversation.ConversationRepository;
import com.xparience.chat.message.Message;
import com.xparience.chat.message.MessageRepository;
import com.xparience.date.dto.CreateDateInviteRequest;
import com.xparience.date.dto.DateInviteResponse;
import com.xparience.date.dto.PlaybackSyncRequest;
import com.xparience.date.dto.PlaybackSyncStateResponse;
import com.xparience.date.dto.PostDateAnalyticsResponse;
import com.xparience.date.dto.RescheduleDateRequest;
import com.xparience.date.dto.StartVideoRoomRequest;
import com.xparience.date.dto.StreamingIntegrationResponse;
import com.xparience.date.dto.VideoRoomResponse;
import com.xparience.matching.MatchRepository;
import com.xparience.matching.MatchStatus;
import com.xparience.profile.ProfileRepository;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateInviteService {

    private final DateInviteRepository dateInviteRepository;
    private final DateSyncEventRepository dateSyncEventRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final MatchRepository matchRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public DateInviteResponse createInvite(CreateDateInviteRequest request) {
        User sender = getCurrentUser();
        User recipient = userRepository.findById(request.getRecipientUserId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        if (sender.getId().equals(recipient.getId())) {
            throw new RuntimeException("You cannot send a date invite to yourself");
        }

        matchRepository.findByUserIds(sender.getId(), recipient.getId())
                .filter(m -> m.getStatus() == MatchStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException(
                        "You can only send date invites to your matches"));

        enforceTwentyFourHourChatRule(sender.getId(), recipient.getId());

        if (request.getScheduledAt() == null || request.getScheduledAt().isBefore(LocalDateTime.now().plusMinutes(15))) {
            throw new RuntimeException("Please choose a schedule at least 15 minutes from now");
        }

        DateInvite invite = new DateInvite();
        invite.setSender(sender);
        invite.setRecipient(recipient);
        invite.setDateType(request.getDateType());
        invite.setStreamingPlatform(request.getStreamingPlatform());
        invite.setTitle(request.getTitle());
        invite.setDescription(request.getDescription());
        invite.setContentLink(request.getContentLink());
        invite.setStreamingContentId(request.getStreamingContentId());
        invite.setScheduledAt(request.getScheduledAt());
        invite.setScreenshotBlockingRequired(request.getScreenshotBlockingRequired() == null || request.getScreenshotBlockingRequired());
        invite.setStatus(DateStatus.PENDING);

        invite = dateInviteRepository.save(invite);
        return toResponse(invite);
    }

    @Transactional
    public DateInviteResponse respondToInvite(Long inviteId, DateStatus response) {
        User user = getCurrentUser();
        DateInvite invite = dateInviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Date invite not found"));

        if (!invite.getRecipient().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to respond to this invite");
        }

        if (invite.getStatus() != DateStatus.PENDING) {
            throw new RuntimeException("This invite has already been responded to");
        }

        if (response != DateStatus.ACCEPTED && response != DateStatus.REJECTED) {
            throw new RuntimeException("Only ACCEPTED or REJECTED responses are allowed");
        }

        invite.setStatus(response);
        invite.setRespondedAt(LocalDateTime.now());
        invite = dateInviteRepository.save(invite);
        return toResponse(invite);
    }

    public List<DateInviteResponse> getDateHistory(Long otherUserId) {
        User user = getCurrentUser();
        return dateInviteRepository.findDateHistory(user.getId(), otherUserId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<DateInviteResponse> getPendingInvites() {
        User user = getCurrentUser();
        return dateInviteRepository.findByRecipientIdAndStatus(user.getId(), DateStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VideoRoomResponse startVideoRoom(Long inviteId, StartVideoRoomRequest request) {
        User user = getCurrentUser();
        DateInvite invite = getInviteForParticipant(inviteId, user.getId());

        if (invite.getStatus() != DateStatus.ACCEPTED && invite.getStatus() != DateStatus.HAPPENING_NOW) {
            throw new RuntimeException("Only accepted invites can start a virtual date room");
        }

        if (invite.getScheduledAt() != null && invite.getScheduledAt().isAfter(LocalDateTime.now().plusMinutes(30))) {
            throw new RuntimeException("Room can only be started within 30 minutes of the scheduled time");
        }

        boolean screenshotAcknowledged = request != null && request.isScreenshotBlockingAcknowledged();
        if (invite.isScreenshotBlockingRequired() && !screenshotAcknowledged) {
            throw new RuntimeException("Screenshot-blocking policy must be acknowledged before entering room");
        }

        if (invite.getWebrtcRoomId() == null || invite.getWebrtcRoomId().isBlank()) {
            invite.setWebrtcRoomId("room-" + UUID.randomUUID());
            invite.setWebrtcJoinToken("join-" + UUID.randomUUID());
            invite.setRoomCreatedAt(LocalDateTime.now());
        }

        if (invite.getDateStartedAt() == null) {
            invite.setDateStartedAt(LocalDateTime.now());
        }

        invite.setStatus(DateStatus.HAPPENING_NOW);
        dateInviteRepository.save(invite);

        VideoRoomResponse response = new VideoRoomResponse();
        response.setInviteId(invite.getId());
        response.setRoomId(invite.getWebrtcRoomId());
        response.setJoinToken(invite.getWebrtcJoinToken());
        response.setRoomCreatedAt(invite.getRoomCreatedAt());
        response.setScreenshotBlockingRequired(invite.isScreenshotBlockingRequired());
        return response;
    }

    @Transactional
    public PlaybackSyncStateResponse syncPlayback(Long inviteId, PlaybackSyncRequest request) {
        User user = getCurrentUser();
        DateInvite invite = getInviteForParticipant(inviteId, user.getId());

        if (invite.getStatus() == DateStatus.PENDING || invite.getStatus() == DateStatus.REJECTED || invite.getStatus() == DateStatus.CANCELLED) {
            throw new RuntimeException("Sync is only available for accepted or active dates");
        }

        if (invite.getDateStartedAt() == null) {
            invite.setDateStartedAt(LocalDateTime.now());
            invite.setStatus(DateStatus.HAPPENING_NOW);
        }

        DateSyncEvent event = new DateSyncEvent();
        event.setInvite(invite);
        event.setActorUserId(user.getId());
        event.setAction(request.getAction());
        event.setPositionSeconds(request.getPositionSeconds());
        dateSyncEventRepository.save(event);

        invite.setLastSyncAction(request.getAction().name());
        invite.setLastSyncPositionSeconds(request.getPositionSeconds());
        invite.setLastSyncAt(LocalDateTime.now());
        invite = dateInviteRepository.save(invite);

        return toSyncState(invite);
    }

    public PlaybackSyncStateResponse getSyncState(Long inviteId) {
        User user = getCurrentUser();
        DateInvite invite = getInviteForParticipant(inviteId, user.getId());
        return toSyncState(invite);
    }

    @Transactional
    public PostDateAnalyticsResponse endDate(Long inviteId) {
        User user = getCurrentUser();
        DateInvite invite = getInviteForParticipant(inviteId, user.getId());

        if (invite.getStatus() == DateStatus.COMPLETED) {
            return buildAnalytics(invite);
        }

        if (invite.getStatus() == DateStatus.CANCELLED || invite.getStatus() == DateStatus.REJECTED || invite.getStatus() == DateStatus.PENDING) {
            throw new RuntimeException("Cannot end a date that has not started");
        }

        invite.setDateEndedAt(LocalDateTime.now());
        invite.setStatus(DateStatus.COMPLETED);
        invite = dateInviteRepository.save(invite);
        return buildAnalytics(invite);
    }

    @Transactional
    public DateInviteResponse reschedule(Long inviteId, RescheduleDateRequest request) {
        User user = getCurrentUser();
        DateInvite invite = getInviteForParticipant(inviteId, user.getId());

        if (invite.getStatus() == DateStatus.COMPLETED || invite.getStatus() == DateStatus.CANCELLED) {
            throw new RuntimeException("Completed or cancelled dates cannot be rescheduled");
        }

        if (invite.getRescheduleCount() >= 3) {
            throw new RuntimeException("Reschedule limit reached for this invite");
        }

        if (request.getNewScheduledAt().isBefore(LocalDateTime.now().plusMinutes(15))) {
            throw new RuntimeException("New schedule must be at least 15 minutes from now");
        }

        invite.setScheduledAt(request.getNewScheduledAt());
        invite.setRescheduleCount(invite.getRescheduleCount() + 1);
        invite.setStatus(DateStatus.ACCEPTED);
        invite = dateInviteRepository.save(invite);
        return toResponse(invite);
    }

    public PostDateAnalyticsResponse getPostDateAnalytics(Long inviteId) {
        User user = getCurrentUser();
        DateInvite invite = getInviteForParticipant(inviteId, user.getId());
        return buildAnalytics(invite);
    }

    public StreamingIntegrationResponse getStreamingIntegration(Long inviteId) {
        User user = getCurrentUser();
        DateInvite invite = getInviteForParticipant(inviteId, user.getId());

        StreamingIntegrationResponse response = new StreamingIntegrationResponse();
        response.setInviteId(invite.getId());
        response.setPlatform(invite.getStreamingPlatform() == null ? "NONE" : invite.getStreamingPlatform().name());
        response.setContentLink(invite.getContentLink());
        response.setProviderContentId(invite.getStreamingContentId());
        response.setIntegrationStatus(invite.getStreamingPlatform() == null ? "NOT_CONFIGURED" : "CONNECTED");
        return response;
    }

    private DateInvite getInviteForParticipant(Long inviteId, Long userId) {
        DateInvite invite = dateInviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Date invite not found"));

        boolean participant = invite.getSender().getId().equals(userId)
                || invite.getRecipient().getId().equals(userId);
        if (!participant) {
            throw new RuntimeException("Unauthorized to access this invite");
        }

        return invite;
    }

    private void enforceTwentyFourHourChatRule(Long senderId, Long recipientId) {
        Conversation conversation = conversationRepository.findByParticipants(senderId, recipientId)
                .orElseThrow(() -> new RuntimeException("Chat with this match must exist before inviting"));

        Message firstMessage = messageRepository.findFirstByConversationIdOrderBySentAtAsc(conversation.getId())
                .orElseThrow(() -> new RuntimeException("Send at least one chat message before inviting"));

        if (firstMessage.getSentAt().isAfter(LocalDateTime.now().minusHours(24))) {
            throw new RuntimeException("Invite button unlocks after 24 hours of chat");
        }
    }

    private PlaybackSyncStateResponse toSyncState(DateInvite invite) {
        PlaybackSyncStateResponse response = new PlaybackSyncStateResponse();
        response.setInviteId(invite.getId());
        response.setLastAction(invite.getLastSyncAction());
        response.setPositionSeconds(invite.getLastSyncPositionSeconds());
        response.setLastSyncedAt(invite.getLastSyncAt());
        return response;
    }

    private PostDateAnalyticsResponse buildAnalytics(DateInvite invite) {
        PostDateAnalyticsResponse response = new PostDateAnalyticsResponse();
        response.setInviteId(invite.getId());

        long durationMinutes = 0;
        if (invite.getDateStartedAt() != null && invite.getDateEndedAt() != null) {
            durationMinutes = Math.max(0, Duration.between(invite.getDateStartedAt(), invite.getDateEndedAt()).toMinutes());
        }

        response.setDurationMinutes(durationMinutes);
        response.setSyncEvents(dateSyncEventRepository.countByInviteId(invite.getId()));
        response.setPauseEvents(dateSyncEventRepository.countByInviteIdAndAction(invite.getId(), SyncAction.PAUSE));
        response.setSeekEvents(dateSyncEventRepository.countByInviteIdAndAction(invite.getId(), SyncAction.SEEK));
        return response;
    }

    private DateInviteResponse toResponse(DateInvite invite) {
        var senderProfile = profileRepository.findByUserId(invite.getSender().getId()).orElse(null);
        var recipientProfile = profileRepository.findByUserId(invite.getRecipient().getId()).orElse(null);

        DateInviteResponse response = new DateInviteResponse();
        response.setInviteId(invite.getId());
        response.setSenderId(invite.getSender().getId());
        response.setSenderName(senderProfile != null ? senderProfile.getFullName()
                : invite.getSender().getEmail());
        response.setRecipientId(invite.getRecipient().getId());
        response.setRecipientName(recipientProfile != null ? recipientProfile.getFullName()
                : invite.getRecipient().getEmail());
        response.setDateType(invite.getDateType());
        response.setStreamingPlatform(invite.getStreamingPlatform());
        response.setTitle(invite.getTitle());
        response.setDescription(invite.getDescription());
        response.setContentLink(invite.getContentLink());
        response.setStreamingContentId(invite.getStreamingContentId());
        response.setStatus(invite.getStatus());
        response.setScheduledAt(invite.getScheduledAt());
        response.setDateStartedAt(invite.getDateStartedAt());
        response.setDateEndedAt(invite.getDateEndedAt());
        response.setWebrtcRoomId(invite.getWebrtcRoomId());
        response.setScreenshotBlockingRequired(invite.isScreenshotBlockingRequired());
        response.setRescheduleCount(invite.getRescheduleCount());
        response.setCreatedAt(invite.getCreatedAt());
        return response;
    }
}