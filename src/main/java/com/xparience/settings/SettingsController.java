package com.xparience.settings;

import com.xparience.common.ApiResponse;
import com.xparience.settings.dto.ChangePasswordRequest;
import com.xparience.settings.dto.SettingsSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "User settings endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SettingsController {

    private final SettingsService settingsService;

    @GetMapping
    @Operation(summary = "Get settings summary (main settings screen)")
    public ResponseEntity<ApiResponse<SettingsSummaryResponse>> getSettingsSummary() {
        return ResponseEntity.ok(ApiResponse.success(
                "Settings", settingsService.getSettingsSummary()));
    }

    @PutMapping("/change-password")
    @Operation(summary = "Change password from settings")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                settingsService.changePassword(request)));
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out current user")
    public ResponseEntity<ApiResponse<String>> logOut() {
        return ResponseEntity.ok(ApiResponse.success(settingsService.logOut()));
    }
}