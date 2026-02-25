package com.xparience.date;

import com.xparience.date.dto.CreateDateInviteRequest;
import com.xparience.date.dto.DateInviteResponse;
import com.xparience.matching.MatchRepository;
import com.xparience.matching.MatchStatus;
import com.xparience.profile.ProfileRepository;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DateInviteService {

    private final DateInviteRepository dateInviteRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final MatchRepository matchRepository;

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

        // Verify they are matched
        matchRepository.findByUserIds(sender.getId(), recipient.getId())
                .filter(m -> m.getStatus() == MatchStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException(
                        "You can only send date invites to your matches"));

        DateInvite invite = new DateInvite();
        invite.setSender(sender);
        invite.setRecipient(recipient);
        invite.setDateType(request.getDateType());
        invite.setStreamingPlatform(request.getStreamingPlatform());
        invite.setTitle(request.getTitle());
        invite.setDescription(request.getDescription());
        invite.setContentLink(request.getContentLink());
        invite.setScheduledAt(request.getScheduledAt());
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
        response.setStatus(invite.getStatus());
        response.setScheduledAt(invite.getScheduledAt());
        response.setCreatedAt(invite.getCreatedAt());
        return response;
    }
}