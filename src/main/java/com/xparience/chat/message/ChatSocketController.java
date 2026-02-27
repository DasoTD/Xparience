package com.xparience.chat.message;

import com.xparience.chat.message.dto.TypingIndicatorRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatSocketController {

    private final MessageService messageService;

    @MessageMapping("/chat/typing")
    public void typing(@Valid TypingIndicatorRequest request) {
        messageService.publishTypingIndicator(request);
    }
}
