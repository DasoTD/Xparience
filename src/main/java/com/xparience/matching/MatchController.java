package com.xparience.matching;

import com.xparience.common.ApiResponse;
import com.xparience.matching.dto.MatchActionRequest;
import com.xparience.matching.dto.MatchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/matches")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "AI-powered matching endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MatchController {

    private final MatchService matchService;

    @GetMapping("/ai-daily")
    @Operation(summary = "Get today's 3 AI curated matches")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getAiDailyMatches() {
        return ResponseEntity.ok(ApiResponse.success(
                "AI daily matches", matchService.getAiDailyMatches()));
    }

    @GetMapping("/accepted")
    @Operation(summary = "Get all accepted matches (for chat list)")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getAcceptedMatches() {
        return ResponseEntity.ok(ApiResponse.success(
                "Accepted matches", matchService.getAcceptedMatches()));
    }

    @PatchMapping("/{matchId}/respond")
    @Operation(summary = "Accept or reject a match")
    public ResponseEntity<ApiResponse<String>> respondToMatch(
            @PathVariable Long matchId,
            @Valid @RequestBody MatchActionRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                matchService.respondToMatch(matchId, request)));
    }
}