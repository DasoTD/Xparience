package com.xparience.chat.message;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.xparience.chat.conversation.Conversation;
import com.xparience.chat.conversation.ConversationRepository;
import com.xparience.chat.conversation.dto.ConversationResponse;
import com.xparience.chat.message.dto.ConversationDeepenerRequest;
import com.xparience.chat.message.dto.ConversationDeepenerResponse;
import com.xparience.chat.message.dto.MessageResponse;
import com.xparience.chat.message.dto.ReportConversationRequest;
import com.xparience.chat.message.dto.SendGifMessageRequest;
import com.xparience.chat.message.dto.SendMessageRequest;
import com.xparience.chat.message.dto.ToneEnhancerRequest;
import com.xparience.chat.message.dto.ToneEnhancerResponse;
import com.xparience.chat.message.dto.TypingIndicatorEvent;
import com.xparience.chat.message.dto.TypingIndicatorRequest;
import com.xparience.matching.MatchRepository;
import com.xparience.matching.MatchStatus;
import com.xparience.profile.ProfileRepository;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

        private static final long MAX_IMAGE_SIZE_BYTES = 8L * 1024 * 1024;
        private static final long MAX_VOICE_SIZE_BYTES = 12L * 1024 * 1024;
        private static final Set<String> NSFW_KEYWORDS = Set.of(
                        "nude", "naked", "sex", "porn", "xxx", "adult", "explicit", "nsfw"
        );

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final MatchRepository matchRepository;
        private final ConversationReportRepository conversationReportRepository;
        private final SimpMessagingTemplate messagingTemplate;
        private final Cloudinary cloudinary;
        private final LocalChatMediaStorageService localChatMediaStorageService;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public MessageResponse sendMessage(SendMessageRequest request) {
        User sender = getCurrentUser();
                User recipient = getRecipient(request.getRecipientUserId());
                Conversation conversation = ensureConversation(sender, recipient);
                return persistAndBroadcast(conversation, sender, request.getContent(), request.getType());
        }

        @Transactional
        public MessageResponse sendMediaMessage(Long recipientUserId, MultipartFile media) {
                if (media == null || media.isEmpty()) {
                        throw new RuntimeException("Media file is required");
                }

                validateImageForChat(media);

                User sender = getCurrentUser();
                User recipient = getRecipient(recipientUserId);
                Conversation conversation = ensureConversation(sender, recipient);

                String mediaUrl = uploadToCloudinary(media, "xparience/chat/media");
                return persistAndBroadcast(conversation, sender, mediaUrl, MessageType.IMAGE);
        }

        @Transactional
        public MessageResponse sendGifMessage(SendGifMessageRequest request) {
                validateGifUrl(request.getGifUrl());

                User sender = getCurrentUser();
                User recipient = getRecipient(request.getRecipientUserId());
                Conversation conversation = ensureConversation(sender, recipient);

                return persistAndBroadcast(conversation, sender, request.getGifUrl().trim(), MessageType.GIF);
        }

        @Transactional
        public MessageResponse sendVoiceNote(Long recipientUserId, MultipartFile voiceNote) {
                if (voiceNote == null || voiceNote.isEmpty()) {
                        throw new RuntimeException("Voice note file is required");
                }

                validateVoiceNote(voiceNote);

                User sender = getCurrentUser();
                User recipient = getRecipient(recipientUserId);
                Conversation conversation = ensureConversation(sender, recipient);

                String voiceUrl = uploadToCloudinary(voiceNote, "xparience/chat/voice");
                return persistAndBroadcast(conversation, sender, voiceUrl, MessageType.VOICE_NOTE);
        }

        public ToneEnhancerResponse enhanceTone(ToneEnhancerRequest request) {
                String normalizedTone = request.getTargetTone().trim().toLowerCase(Locale.ROOT);
                String message = request.getMessage().trim();

                String enhanced = switch (normalizedTone) {
                        case "friendly" -> "Hey 😊 " + capitalize(message);
                        case "flirty" -> "Hey you 😉 " + message + "";
                        case "confident" -> "I'd love this: " + capitalize(message);
                        case "empathetic" -> "I hear you. " + capitalize(message);
                        default -> capitalize(message);
                };

                ToneEnhancerResponse response = new ToneEnhancerResponse();
                response.setOriginalMessage(request.getMessage());
                response.setTargetTone(request.getTargetTone());
                response.setEnhancedMessage(enhanced);
                return response;
        }

        public ConversationDeepenerResponse generateConversationDeepener(ConversationDeepenerRequest request) {
                User currentUser = getCurrentUser();
                Conversation conversation = getConversationForParticipant(request.getConversationId(), currentUser);

                List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(conversation.getId());
                String anchor = determineAnchorTopic(request.getContextTopic(), messages);

                List<String> prompts = new ArrayList<>();
                prompts.add("What about " + anchor + " matters most to you personally?");
                prompts.add("Has your view on " + anchor + " changed over time?");
                prompts.add("What's one experience that shaped how you think about " + anchor + "?");
                prompts.add("What would your ideal next step around " + anchor + " look like?");

                ConversationDeepenerResponse response = new ConversationDeepenerResponse();
                response.setConversationId(conversation.getId());
                response.setAnchorTopic(anchor);
                response.setSuggestedPrompts(prompts);
                return response;
        }

        @Transactional
        public String blockConversation(Long conversationId) {
                User user = getCurrentUser();
                Conversation conversation = getConversationForParticipant(conversationId, user);

                if (conversation.getParticipantOne().getId().equals(user.getId())) {
                        conversation.setBlockedByOne(true);
                } else {
                        conversation.setBlockedByTwo(true);
                }

                conversationRepository.save(conversation);
                return "Conversation blocked";
        }

        @Transactional
        public String unblockConversation(Long conversationId) {
                User user = getCurrentUser();
                Conversation conversation = getConversationForParticipant(conversationId, user);

                if (conversation.getParticipantOne().getId().equals(user.getId())) {
                        conversation.setBlockedByOne(false);
                } else {
                        conversation.setBlockedByTwo(false);
                }

                conversationRepository.save(conversation);
                return "Conversation unblocked";
        }

        @Transactional
        public String reportConversation(Long conversationId, ReportConversationRequest request) {
                User reporter = getCurrentUser();
                Conversation conversation = getConversationForParticipant(conversationId, reporter);

                User reportedUser = conversation.getParticipantOne().getId().equals(reporter.getId())
                                ? conversation.getParticipantTwo()
                                : conversation.getParticipantOne();

                ConversationReport report = new ConversationReport();
                report.setConversation(conversation);
                report.setReporter(reporter);
                report.setReportedUser(reportedUser);
                report.setReason(request.getReason());
                report.setDetails(request.getDetails());

                conversationReportRepository.save(report);
                return "Conversation reported";
        }

        public void publishTypingIndicator(TypingIndicatorRequest request) {
                User currentUser = getCurrentUser();
                getConversationForParticipant(request.getConversationId(), currentUser);

                TypingIndicatorEvent event = new TypingIndicatorEvent();
                event.setConversationId(request.getConversationId());
                event.setUserId(currentUser.getId());
                event.setTyping(request.isTyping());
                event.setTimestamp(LocalDateTime.now());

                messagingTemplate.convertAndSend("/topic/chat/" + request.getConversationId() + "/typing", event);
    }

        @Transactional
        public List<MessageResponse> getMessages(Long conversationId) {
        User user = getCurrentUser();
        getConversationForParticipant(conversationId, user);

        messageRepository.markAllAsDelivered(conversationId, user.getId());
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
                response.setDelivered(message.isDelivered());
                response.setRead(message.isRead());
                response.setSentAt(message.getSentAt());
                response.setDeliveredAt(message.getDeliveredAt());
                response.setReadAt(message.getReadAt());
                return response;
    }

        private Conversation ensureConversation(User sender, User recipient) {
                ensureUsersAreMatched(sender, recipient);

                Conversation conversation = conversationRepository
                                .findByParticipants(sender.getId(), recipient.getId())
                                .orElseGet(() -> {
                                        Conversation newConversation = new Conversation();
                                        newConversation.setParticipantOne(sender);
                                        newConversation.setParticipantTwo(recipient);
                                        return conversationRepository.save(newConversation);
                                });

                if (isBlockedBySender(conversation, sender)) {
                        throw new RuntimeException("You cannot send messages in this conversation");
                }

                return conversation;
        }

        private MessageResponse persistAndBroadcast(Conversation conversation,
                                                                                                User sender,
                                                                                                String content,
                                                                                                MessageType messageType) {
                Message message = new Message();
                message.setConversation(conversation);
                message.setSender(sender);
                message.setContent(content);
                message.setType(messageType);

                message = messageRepository.save(message);

                conversation.setLastMessage(messageType == MessageType.TEXT ? content : messageType.name());
                conversation.setLastMessageAt(LocalDateTime.now());
                conversationRepository.save(conversation);

                MessageResponse response = toMessageResponse(message);
                messagingTemplate.convertAndSend("/topic/chat/" + conversation.getId() + "/messages", response);
                return response;
        }

        private void ensureUsersAreMatched(User sender, User recipient) {
                matchRepository.findByUserIds(sender.getId(), recipient.getId())
                                .filter(m -> m.getStatus() == MatchStatus.ACCEPTED)
                                .orElseThrow(() -> new RuntimeException("You can only message users you are matched with"));
        }

        private User getRecipient(Long recipientUserId) {
                return userRepository.findById(recipientUserId)
                                .orElseThrow(() -> new RuntimeException("Recipient not found"));
        }

        private boolean isBlockedBySender(Conversation conversation, User sender) {
                return (conversation.getParticipantOne().getId().equals(sender.getId()) && conversation.isBlockedByOne())
                                || (conversation.getParticipantTwo().getId().equals(sender.getId()) && conversation.isBlockedByTwo());
        }

        private void validateImageForChat(MultipartFile media) {
                String contentType = media.getContentType();
                if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                        throw new RuntimeException("Only image media is supported for chat uploads");
                }

                if (media.getSize() > MAX_IMAGE_SIZE_BYTES) {
                        throw new RuntimeException("Image must be less than 8MB");
                }

                String fileName = media.getOriginalFilename() == null ? "" : media.getOriginalFilename().toLowerCase(Locale.ROOT);
                if (containsNsfwKeyword(fileName)) {
                        throw new RuntimeException("Media rejected by NSFW filter");
                }
        }

        private void validateGifUrl(String gifUrl) {
                String normalized = gifUrl == null ? "" : gifUrl.trim().toLowerCase(Locale.ROOT);
                if (!(normalized.startsWith("http://") || normalized.startsWith("https://"))) {
                        throw new RuntimeException("GIF URL must start with http:// or https://");
                }

                if (!normalized.endsWith(".gif") && !normalized.contains("giphy.com") && !normalized.contains("tenor.com")) {
                        throw new RuntimeException("Unsupported GIF source");
                }

                if (containsNsfwKeyword(normalized)) {
                        throw new RuntimeException("GIF rejected by NSFW filter");
                }
        }

        private void validateVoiceNote(MultipartFile voiceNote) {
                String contentType = voiceNote.getContentType();
                if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("audio/")) {
                        throw new RuntimeException("Only audio files are allowed for voice notes");
                }

                if (voiceNote.getSize() > MAX_VOICE_SIZE_BYTES) {
                        throw new RuntimeException("Voice note must be less than 12MB");
                }

                String filename = voiceNote.getOriginalFilename() == null ? "" : voiceNote.getOriginalFilename().toLowerCase(Locale.ROOT);
                if (containsNsfwKeyword(filename)) {
                        throw new RuntimeException("Voice note rejected by NSFW filter");
                }
        }

        private String determineAnchorTopic(String contextTopic, List<Message> messages) {
                if (contextTopic != null && !contextTopic.isBlank()) {
                        return contextTopic.trim();
                }

                for (int i = messages.size() - 1; i >= 0; i--) {
                        Message candidate = messages.get(i);
                        if (candidate.getType() == MessageType.TEXT && candidate.getContent() != null && !candidate.getContent().isBlank()) {
                                String content = candidate.getContent().trim();
                                return content.length() > 32 ? content.substring(0, 32) + "..." : content;
                        }
                }

                return "your goals";
        }

        private String capitalize(String value) {
                if (value == null || value.isBlank()) {
                        return value;
                }
                return Character.toUpperCase(value.charAt(0)) + value.substring(1);
        }

        private boolean containsNsfwKeyword(String value) {
                for (String keyword : NSFW_KEYWORDS) {
                        if (value.contains(keyword)) {
                                return true;
                        }
                }
                return false;
        }

        private String uploadToCloudinary(MultipartFile file, String folder) {
                try {
                        Map<?, ?> result = cloudinary.uploader().upload(
                                        file.getBytes(), ObjectUtils.asMap("folder", folder));
                        Object secureUrl = result.get("secure_url");
                        if (secureUrl != null) {
                                return secureUrl.toString();
                        }

                        return buildLocalFallbackUrl(file, folder);
                } catch (Exception ex) {
                        return buildLocalFallbackUrl(file, folder);
                }
        }

        private String buildLocalFallbackUrl(MultipartFile file, String folder) {
                String storedName = localChatMediaStorageService.store(file, folder);
                return "/api/v1/chat/media/local/" + storedName;
        }

        private Conversation getConversationForParticipant(Long conversationId, User user) {
                Conversation conversation = conversationRepository.findById(conversationId)
                                .orElseThrow(() -> new RuntimeException("Conversation not found"));

                if (!conversation.getParticipantOne().getId().equals(user.getId())
                                && !conversation.getParticipantTwo().getId().equals(user.getId())) {
                        throw new RuntimeException("Unauthorized to access this conversation");
                }

                return conversation;
        }
}