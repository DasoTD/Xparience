package com.xparience.admin;

import com.xparience.admin.dto.AdminAnalyticsResponse;
import com.xparience.admin.dto.AdminReportResponse;
import com.xparience.admin.dto.AdminUserSummaryResponse;
import com.xparience.admin.dto.AdminVerificationResponse;
import com.xparience.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin moderation and analytics endpoints")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "View users list")
    public ResponseEntity<ApiResponse<List<AdminUserSummaryResponse>>> getUsers() {
        return ResponseEntity.ok(ApiResponse.success("Users", adminService.getUsers()));
    }

    @PatchMapping("/users/{userId}/suspend")
    @Operation(summary = "Suspend a user account")
    public ResponseEntity<ApiResponse<String>> suspendUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(adminService.suspendUser(userId)));
    }

    @GetMapping("/verifications/pending")
    @Operation(summary = "View verification records pending manual review")
    public ResponseEntity<ApiResponse<List<AdminVerificationResponse>>> getPendingVerifications() {
        return ResponseEntity.ok(ApiResponse.success("Pending verifications", adminService.getPendingVerifications()));
    }

    @PatchMapping("/verifications/{verificationId}/review")
    @Operation(summary = "Approve or reject a verification record")
    public ResponseEntity<ApiResponse<String>> reviewVerification(
            @PathVariable Long verificationId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(ApiResponse.success(adminService.reviewVerification(verificationId, approved, note)));
    }

    @GetMapping("/reports")
    @Operation(summary = "View reported users")
    public ResponseEntity<ApiResponse<List<AdminReportResponse>>> getReportedUsers() {
        return ResponseEntity.ok(ApiResponse.success("Reported users", adminService.getReportedUsers()));
    }

    @GetMapping("/analytics")
    @Operation(summary = "Get basic admin analytics")
    public ResponseEntity<ApiResponse<AdminAnalyticsResponse>> getAnalytics() {
        return ResponseEntity.ok(ApiResponse.success("Analytics", adminService.getAnalytics()));
    }
}
