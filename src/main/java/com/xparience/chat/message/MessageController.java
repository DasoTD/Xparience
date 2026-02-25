package com.xparience.chat.message;

import com.xparience.chat.conversation.dto.ConversationResponse;
import com.xparience.chat.message.dto.MessageResponse;
import com.xparience.chat.message.dto.SendMessageRequest;
import com.xparience.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}