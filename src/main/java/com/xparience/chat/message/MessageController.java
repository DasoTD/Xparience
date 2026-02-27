package com.xparience.chat.message;

import com.xparience.chat.conversation.dto.ConversationResponse;
import com.xparience.chat.message.dto.ConversationDeepenerRequest;
import com.xparience.chat.message.dto.ConversationDeepenerResponse;
import com.xparience.chat.message.dto.MessageResponse;
import com.xparience.chat.message.dto.ReportConversationRequest;
import com.xparience.chat.message.dto.SendGifMessageRequest;
import com.xparience.chat.message.dto.SendMessageRequest;
import com.xparience.chat.message.dto.ToneEnhancerRequest;
import com.xparience.chat.message.dto.ToneEnhancerResponse;
import com.xparience.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Messaging endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/conversations")
    @Operation(summary = "Get all conversations (chat list screen)")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getConversations() {
        return ResponseEntity.ok(ApiResponse.success(
                "Conversations", messageService.getConversations()));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Get all messages in a conversation")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getMessages(
            @PathVariable Long conversationId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Messages", messageService.getMessages(conversationId)));
    }

    @PostMapping("/send")
    @Operation(summary = "Send a message")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Message sent", messageService.sendMessage(request)));
    }

        @PostMapping(value = "/send-media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        @Operation(summary = "Send media message (image upload with NSFW filter)")
        public ResponseEntity<ApiResponse<MessageResponse>> sendMediaMessage(
                @RequestParam("recipientUserId") Long recipientUserId,
            @RequestPart("media") MultipartFile media) {
        return ResponseEntity.ok(ApiResponse.success(
            "Media message sent", messageService.sendMediaMessage(recipientUserId, media)));
        }

        @PostMapping("/send-gif")
        @Operation(summary = "Send GIF message")
        public ResponseEntity<ApiResponse<MessageResponse>> sendGifMessage(
            @Valid @RequestBody SendGifMessageRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
            "GIF message sent", messageService.sendGifMessage(request)));
        }

            @PostMapping(value = "/send-voice", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
            @Operation(summary = "Send voice note message")
            public ResponseEntity<ApiResponse<MessageResponse>> sendVoiceMessage(
                    @RequestParam("recipientUserId") Long recipientUserId,
                @RequestPart("voiceNote") MultipartFile voiceNote) {
            return ResponseEntity.ok(ApiResponse.success(
                "Voice note sent", messageService.sendVoiceNote(recipientUserId, voiceNote)));
            }

            @PostMapping("/tone-enhancer")
            @Operation(summary = "Enhance a message tone before sending")
            public ResponseEntity<ApiResponse<ToneEnhancerResponse>> toneEnhancer(
                @Valid @RequestBody ToneEnhancerRequest request) {
            return ResponseEntity.ok(ApiResponse.success(
                "Tone enhancement generated", messageService.enhanceTone(request)));
            }

            @PostMapping("/conversation-deepener")
            @Operation(summary = "Generate deeper conversation prompts")
            public ResponseEntity<ApiResponse<ConversationDeepenerResponse>> conversationDeepener(
                @Valid @RequestBody ConversationDeepenerRequest request) {
            return ResponseEntity.ok(ApiResponse.success(
                "Conversation prompts generated", messageService.generateConversationDeepener(request)));
            }

    @PatchMapping("/conversations/{conversationId}/block")
    @Operation(summary = "Block a conversation")
    public ResponseEntity<ApiResponse<String>> blockConversation(@PathVariable Long conversationId) {
        return ResponseEntity.ok(ApiResponse.success(messageService.blockConversation(conversationId)));
    }

    @PatchMapping("/conversations/{conversationId}/unblock")
    @Operation(summary = "Unblock a conversation")
    public ResponseEntity<ApiResponse<String>> unblockConversation(@PathVariable Long conversationId) {
        return ResponseEntity.ok(ApiResponse.success(messageService.unblockConversation(conversationId)));
    }

    @PostMapping("/conversations/{conversationId}/report")
    @Operation(summary = "Report a conversation participant")
    public ResponseEntity<ApiResponse<String>> reportConversation(@PathVariable Long conversationId,
                                                                  @Valid @RequestBody ReportConversationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(messageService.reportConversation(conversationId, request)));
    }
}