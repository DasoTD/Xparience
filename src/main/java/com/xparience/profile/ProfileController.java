package com.xparience.profile;

import com.xparience.common.ApiResponse;
import com.xparience.profile.dto.*;
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
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Profile creation and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/basic-info")
    @Operation(summary = "Step 1 — Save basic information")
    public ResponseEntity<ApiResponse<String>> saveBasicInfo(
            @Valid @RequestBody BasicInfoRequest request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.saveBasicInfo(request)));
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Step 2 — Upload profile and gallery images")
    public ResponseEntity<ApiResponse<String>> uploadImages(
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "additionalImages", required = false) List<MultipartFile> additionalImages) {
        return ResponseEntity.ok(ApiResponse.success(
                profileService.uploadImages(profilePicture, additionalImages)));
    }

    @PostMapping("/about-you")
    @Operation(summary = "Step 3 — Save bio and values")
    public ResponseEntity<ApiResponse<String>> saveAboutYou(
            @Valid @RequestBody AboutYouRequest request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.saveAboutYou(request)));
    }

    @PostMapping("/preferences")
    @Operation(summary = "Step 4 — Save preferences (things you love)")
    public ResponseEntity<ApiResponse<String>> savePreferences(
            @Valid @RequestBody PreferencesRequest request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.savePreferences(request)));
    }

    @PostMapping("/non-negotiables")
    @Operation(summary = "Step 5 — Save matching criteria / non-negotiables")
    public ResponseEntity<ApiResponse<String>> saveNonNegotiables(
            @Valid @RequestBody NonNegotiablesRequest request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.saveNonNegotiables(request)));
    }

    @PostMapping("/nutrition-vibe")
    @Operation(summary = "Step 6 — Save nutrition and lifestyle info")
    public ResponseEntity<ApiResponse<String>> saveNutritionVibe(
            @Valid @RequestBody NutritionVibeRequest request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.saveNutritionVibe(request)));
    }

    @PostMapping("/personality-quiz")
    @Operation(summary = "Step 7 — Save personality quiz answers")
    public ResponseEntity<ApiResponse<String>> savePersonalityQuiz(
            @Valid @RequestBody PersonalityQuizRequest request) {
        return ResponseEntity.ok(ApiResponse.success(profileService.savePersonalityQuiz(request)));
    }

    @GetMapping("/completion-status")
    @Operation(summary = "Get profile completion status (Welcome Back screen)")
    public ResponseEntity<ApiResponse<ProfileCompletionResponse>> getCompletionStatus() {
        return ResponseEntity.ok(ApiResponse.success("Profile completion status",
                profileService.getCompletionStatus()));
    }

    @PostMapping("/verify-identity")
    @Operation(summary = "Final step — Verify identity")
    public ResponseEntity<ApiResponse<String>> verifyIdentity() {
        return ResponseEntity.ok(ApiResponse.success(profileService.verifyIdentity()));
    }
}