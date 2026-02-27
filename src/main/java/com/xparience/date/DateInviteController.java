package com.xparience.date;

import com.xparience.common.ApiResponse;
import com.xparience.date.dto.CreateDateInviteRequest;
import com.xparience.date.dto.DateInviteResponse;
import com.xparience.date.dto.PlaybackSyncRequest;
import com.xparience.date.dto.PlaybackSyncStateResponse;
import com.xparience.date.dto.PostDateAnalyticsResponse;
import com.xparience.date.dto.RescheduleDateRequest;
import com.xparience.date.dto.StartVideoRoomRequest;
import com.xparience.date.dto.StreamingIntegrationResponse;
import com.xparience.date.dto.VideoRoomResponse;
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

    @PostMapping("/{inviteId}/start-room")
    @Operation(summary = "Start or join WebRTC room for accepted virtual date")
    public ResponseEntity<ApiResponse<VideoRoomResponse>> startRoom(
            @PathVariable Long inviteId,
            @RequestBody(required = false) StartVideoRoomRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Video room ready", dateInviteService.startVideoRoom(inviteId, request)));
    }

    @PostMapping("/{inviteId}/sync")
    @Operation(summary = "Send synchronized play/pause/seek event")
    public ResponseEntity<ApiResponse<PlaybackSyncStateResponse>> syncPlayback(
            @PathVariable Long inviteId,
            @Valid @RequestBody PlaybackSyncRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sync event processed", dateInviteService.syncPlayback(inviteId, request)));
    }

    @GetMapping("/{inviteId}/sync-state")
    @Operation(summary = "Get latest synchronized playback state")
    public ResponseEntity<ApiResponse<PlaybackSyncStateResponse>> getSyncState(
            @PathVariable Long inviteId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sync state", dateInviteService.getSyncState(inviteId)));
    }

    @PostMapping("/{inviteId}/end")
    @Operation(summary = "End virtual date")
    public ResponseEntity<ApiResponse<PostDateAnalyticsResponse>> endDate(
            @PathVariable Long inviteId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Date ended", dateInviteService.endDate(inviteId)));
    }

    @PostMapping("/{inviteId}/reschedule")
    @Operation(summary = "Reschedule date")
    public ResponseEntity<ApiResponse<DateInviteResponse>> rescheduleDate(
            @PathVariable Long inviteId,
            @Valid @RequestBody RescheduleDateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Date rescheduled", dateInviteService.reschedule(inviteId, request)));
    }

    @GetMapping("/{inviteId}/analytics")
    @Operation(summary = "Get post-date analytics")
    public ResponseEntity<ApiResponse<PostDateAnalyticsResponse>> getAnalytics(
            @PathVariable Long inviteId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Post-date analytics", dateInviteService.getPostDateAnalytics(inviteId)));
    }

    @GetMapping("/{inviteId}/streaming")
    @Operation(summary = "Get streaming platform integration context")
    public ResponseEntity<ApiResponse<StreamingIntegrationResponse>> getStreamingIntegration(
            @PathVariable Long inviteId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Streaming integration", dateInviteService.getStreamingIntegration(inviteId)));
    }
}