package com.xparience.chat.ai;

import com.xparience.chat.ai.dto.AiSuggestionResponse;
import com.xparience.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
@Tag(name = "AI Suggestions", description = "AI-powered date and chat suggestions")
@SecurityRequirement(name = "bearerAuth")
public class AiSuggestionController {

    private final AiSuggestionService aiSuggestionService;

    @GetMapping("/date-suggestions/{matchedUserId}")
    @Operation(summary = "Get AI physical date suggestions based on shared profile")
    public ResponseEntity<ApiResponse<List<AiSuggestionResponse>>> getPhysicalDateSuggestions(
            @PathVariable Long matchedUserId) {
        return ResponseEntity.ok(ApiResponse.success(
                "AI date suggestions",
                aiSuggestionService.getPhysicalDateSuggestions(matchedUserId)));
    }

    @GetMapping("/virtual-date-options")
    @Operation(summary = "Get virtual date activity options")
    public ResponseEntity<ApiResponse<List<AiSuggestionResponse>>> getVirtualDateOptions() {
        return ResponseEntity.ok(ApiResponse.success(
                "Virtual date options",
                aiSuggestionService.getVirtualDateOptions()));
    }
}