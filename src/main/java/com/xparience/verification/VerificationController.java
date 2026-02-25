package com.xparience.verification;

import com.xparience.common.ApiResponse;
import com.xparience.verification.dto.GovernmentIdRequest;
import com.xparience.verification.dto.VerificationStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/verification")
@RequiredArgsConstructor
@Tag(name = "Verification", description = "ID and Face verification endpoints")
@SecurityRequirement(name = "bearerAuth")
public class VerificationController {

    private final VerificationService verificationService;

    @PostMapping(value = "/government-id", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Step 1 — Submit Government ID details and document")
    public ResponseEntity<ApiResponse<String>> submitGovernmentId(
            @RequestPart("data") @Valid GovernmentIdRequest request,
            @RequestPart(value = "document", required = false) MultipartFile document) {
        return ResponseEntity.ok(ApiResponse.success(
                verificationService.submitGovernmentId(request, document)));
    }

    @PostMapping(value = "/selfie", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Step 2 — Submit selfie for real-time face match")
    public ResponseEntity<ApiResponse<String>> submitSelfie(
            @RequestPart("selfie") MultipartFile selfie) {
        return ResponseEntity.ok(ApiResponse.success(
                verificationService.submitSelfie(selfie)));
    }

    @GetMapping("/status")
    @Operation(summary = "Get current verification status")
    public ResponseEntity<ApiResponse<VerificationStatusResponse>> getStatus() {
        return ResponseEntity.ok(ApiResponse.success(
                "Verification status", verificationService.getVerificationStatus()));
    }
}