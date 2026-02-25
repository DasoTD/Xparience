package com.xparience.user;

import com.xparience.common.ApiResponse;
import com.xparience.user.dto.UpdateProfileRequest;
import com.xparience.user.dto.UserInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Information", description = "View and edit user profile information")
@SecurityRequirement(name = "bearerAuth")
public class UserInfoController {

    private final UserInfoService userInfoService;

    @GetMapping("/info")
    @Operation(summary = "Get full user profile info (User Information screen)")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo() {
        return ResponseEntity.ok(ApiResponse.success(
                "User information", userInfoService.getUserInfo()));
    }

    @PutMapping("/info")
    @Operation(summary = "Update profile information (Profile Edit screens 1-7)")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateProfile(
            @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Profile Updated!!", userInfoService.updateProfile(request)));
    }

    @PutMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update profile and gallery images (Profile Edit 2)")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateImages(
            @RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture,
            @RequestPart(value = "additionalImages", required = false) List<MultipartFile> additionalImages) {
        return ResponseEntity.ok(ApiResponse.success(
                "Images updated successfully",
                userInfoService.updateImages(profilePicture, additionalImages)));
    }
}