package com.xparience.chat.message;

import com.xparience.chat.conversation.Conversation;
import com.xparience.chat.conversation.ConversationRepository;
import com.xparience.chat.conversation.dto.ConversationResponse;
import com.xparience.chat.message.dto.MessageResponse;
import com.xparience.chat.message.dto.SendMessageRequest;
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
public class MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final MatchRepository matchRepository;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        User sender = getCurrentUser();
        User recipient = userRepository.findById(request.getRecipientUserId())
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        // Verify they are matched
        matchRepository.findByUserIds(sender.getId(), recipient.getId())
                .filter(m -> m.getStatus() == MatchStatus.ACCEPTED)
                .orElseThrow(() -> new RuntimeException(
                        "You can only message users you are matched with"));

        // Get or create conversation
        Conversation conversation = conversationRepository
                .findByParticipants(sender.getId(), recipient.getId())
                .orElseGet(() -> {
                    Conversation newConversation = new Conversation();
                    newConversation.setParticipantOne(sender);
                    newConversation.setParticipantTwo(recipient);
                    return conversationRepository.save(newConversation);
                });

        // Check if blocked
        boolean isBlocked = (conversation.getParticipantOne().getId().equals(sender.getId())
                && conversation.isBlockedByOne())
                || (conversation.getParticipantTwo().getId().equals(sender.getId())
                && conversation.isBlockedByTwo());

        if (isBlocked) {
            throw new RuntimeException("You cannot send messages in this conversation");
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setType(request.getType());

        message = messageRepository.save(message);

        // Update conversation last message
        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        return toMessageResponse(message);
    }

    public List<MessageResponse> getMessages(Long conversationId) {
        User user = getCurrentUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        // Verify user is a participant
        if (!conversation.getParticipantOne().getId().equals(user.getId()) &&
            !conversation.getParticipantTwo().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized to view this conversation");
        }

        messageRepository.markAllAsRead(conversationId, user.getId());

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId)
                .stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    public List<ConversationResponse> getConversations() {
        User user = getCurrentUser();
        List<Conversation> conversations = conversationRepository.findAllByUserId(user.getId());

        return conversations.stream()
                .map(conv -> {
                    User other = conv.getParticipantOne().getId().equals(user.getId())
                            ? conv.getParticipantTwo()
                            : conv.getParticipantOne();

                    var profile = profileRepository.findByUserId(other.getId()).orElse(null);
                    int unread = messageRepository.countUnreadMessages(conv.getId(), user.getId());

                    boolean isBlocked = (conv.getParticipantOne().getId().equals(user.getId())
                            && conv.isBlockedByOne())
                            || (conv.getParticipantTwo().getId().equals(user.getId())
                            && conv.isBlockedByTwo());

                                        ConversationResponse response = new ConversationResponse();
                                        response.setConversationId(conv.getId());
                                        response.setOtherUserId(other.getId());
                                        response.setOtherUserName(profile != null ? profile.getFullName() : other.getEmail());
                                        response.setOtherUserProfilePicture(profile != null ? profile.getProfilePictureUrl() : null);
                                        response.setLastMessage(conv.getLastMessage());
                                        response.setLastMessageAt(conv.getLastMessageAt());
                                        response.setUnreadCount(unread);
                                        response.setBlocked(isBlocked);
                                        return response;
                })
                .collect(Collectors.toList());
    }

    private MessageResponse toMessageResponse(Message message) {
        var profile = profileRepository.findByUserId(message.getSender().getId()).orElse(null);
                MessageResponse response = new MessageResponse();
                response.setMessageId(message.getId());
                response.setConversationId(message.getConversation().getId());
                response.setSenderId(message.getSender().getId());
                response.setSenderName(profile != null ? profile.getFullName() : message.getSender().getEmail());
                response.setContent(message.getContent());
                response.setType(message.getType());
                response.setRead(message.isRead());
                response.setSentAt(message.getSentAt());
                return response;
    }
}