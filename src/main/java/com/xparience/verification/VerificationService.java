package com.xparience.verification;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.xparience.profile.Profile;
import com.xparience.profile.ProfileRepository;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import com.xparience.verification.dto.GovernmentIdRequest;
import com.xparience.verification.dto.VerificationStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final Cloudinary cloudinary;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public String submitGovernmentId(GovernmentIdRequest request, MultipartFile document) {
        User user = getCurrentUser();

        VerificationRecord record = verificationRepository
                .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseGet(() -> {
                    VerificationRecord newRecord = new VerificationRecord();
                    newRecord.setUser(user);
                    return newRecord;
                });

        record.setIdType(request.getIdType());
        record.setIdNumber(request.getIdNumber());

        if (document != null && !document.isEmpty()) {
            String docUrl = uploadToCloudinary(document, "xparience/verification/ids");
            record.setIdDocumentUrl(docUrl);
        }

        record.setStatus(VerificationStatus.PENDING);
        verificationRepository.save(record);

        return "Government ID submitted successfully. Please proceed to facial verification.";
    }

    @Transactional
    public String submitSelfie(MultipartFile selfie) {
        User user = getCurrentUser();

        VerificationRecord record = verificationRepository
                .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                .orElseThrow(() -> new RuntimeException(
                        "Please complete Government ID submission first"));

        if (selfie == null || selfie.isEmpty()) {
            throw new RuntimeException("Selfie image is required");
        }

        String selfieUrl = uploadToCloudinary(selfie, "xparience/verification/selfies");
        record.setSelfieImageUrl(selfieUrl);

        // Placeholder face match logic
        // In production: integrate with AWS Rekognition / Azure Face API / Jumio
        double mockConfidence = 92.5;
        record.setFaceMatchPassed(mockConfidence >= 80.0);
        record.setFaceMatchConfidence(mockConfidence);
        record.setStatus(VerificationStatus.UNDER_REVIEW);
        record.setSubmittedAt(LocalDateTime.now());

        verificationRepository.save(record);

        // Update profile
        profileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            profile.setIdentityVerified(false); // Will be true after admin approval
            profileRepository.save(profile);
        });

        return "Facial verification submitted. Your identity is under review. " +
               "You will be notified once approved.";
    }

    public VerificationStatusResponse getVerificationStatus() {
        User user = getCurrentUser();

        return verificationRepository.findTopByUserIdOrderByCreatedAtDesc(user.getId())
            .map(record -> {
                VerificationStatusResponse response = new VerificationStatusResponse();
                response.setVerificationId(record.getId());
                response.setIdVerificationComplete(record.getIdDocumentUrl() != null);
                response.setFaceVerificationComplete(record.getSelfieImageUrl() != null);
                response.setFaceMatchPassed(record.isFaceMatchPassed());
                response.setFaceMatchConfidence(record.getFaceMatchConfidence());
                response.setStatus(record.getStatus());
                response.setReviewNote(record.getReviewNote());
                response.setSubmittedAt(record.getSubmittedAt());
                return response;
            })
            .orElseGet(() -> {
                VerificationStatusResponse response = new VerificationStatusResponse();
                response.setIdVerificationComplete(false);
                response.setFaceVerificationComplete(false);
                response.setFaceMatchPassed(false);
                response.setStatus(VerificationStatus.PENDING);
                return response;
            });
    }

    private String uploadToCloudinary(MultipartFile file, String folder) {
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", folder)
            );
            return result.get("secure_url").toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage());
        }
    }
}