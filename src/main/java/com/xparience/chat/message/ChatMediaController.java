package com.xparience.chat.message;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat/media/local")
@RequiredArgsConstructor
@Tag(name = "Chat Media", description = "Local fallback media streaming endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ChatMediaController {

    private final LocalChatMediaStorageService localChatMediaStorageService;

    @GetMapping("/{fileName:.+}")
    @Operation(summary = "Get locally stored chat media fallback file")
    public ResponseEntity<Resource> getLocalMedia(@PathVariable String fileName) {
        Resource resource = localChatMediaStorageService.load(fileName);
        String contentType = localChatMediaStorageService.resolveContentType(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}
