package com.xparience.date;

import com.xparience.common.ApiResponse;
import com.xparience.date.dto.CreateDateInviteRequest;
import com.xparience.date.dto.DateInviteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dates")
@RequiredArgsConstructor
@Tag(name = "Virtual & Physical Dates", description = "Date invite management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class DateInviteController {

    private final DateInviteService dateInviteService;

    @PostMapping("/invite")
    @Operation(summary = "Send a virtual or physical date invite")
    public ResponseEntity<ApiResponse<DateInviteResponse>> createInvite(
            @Valid @RequestBody CreateDateInviteRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Date invite sent!", dateInviteService.createInvite(request)));
    }

    @PatchMapping("/{inviteId}/respond")
    @Operation(summary = "Accept or reject a date invite")
    public ResponseEntity<ApiResponse<DateInviteResponse>> respondToInvite(
            @PathVariable Long inviteId,
            @RequestParam DateStatus response) {
        return ResponseEntity.ok(ApiResponse.success(
                "Response recorded", dateInviteService.respondToInvite(inviteId, response)));
    }

    @GetMapping("/history/{otherUserId}")
    @Operation(summary = "Get date history with a specific user")
    public ResponseEntity<ApiResponse<List<DateInviteResponse>>> getDateHistory(
            @PathVariable Long otherUserId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Date history", dateInviteService.getDateHistory(otherUserId)));
    }

    @GetMapping("/pending")
    @Operation(summary = "Get all pending date invites")
    public ResponseEntity<ApiResponse<List<DateInviteResponse>>> getPendingInvites() {
        return ResponseEntity.ok(ApiResponse.success(
                "Pending invites", dateInviteService.getPendingInvites()));
    }
}