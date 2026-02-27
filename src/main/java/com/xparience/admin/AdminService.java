package com.xparience.admin;

import com.xparience.admin.dto.AdminAnalyticsResponse;
import com.xparience.admin.dto.AdminReportResponse;
import com.xparience.admin.dto.AdminUserSummaryResponse;
import com.xparience.admin.dto.AdminVerificationResponse;
import com.xparience.chat.message.ConversationReport;
import com.xparience.chat.message.ConversationReportRepository;
import com.xparience.matching.MatchRepository;
import com.xparience.profile.ProfileRepository;
import com.xparience.user.User;
import com.xparience.user.UserRepository;
import com.xparience.verification.VerificationRecord;
import com.xparience.verification.VerificationRepository;
import com.xparience.verification.VerificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final ConversationReportRepository conversationReportRepository;
    private final MatchRepository matchRepository;
    private final ProfileRepository profileRepository;

    public List<AdminUserSummaryResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserSummary)
                .toList();
    }

    public List<AdminVerificationResponse> getPendingVerifications() {
        return verificationRepository.findAllByStatusOrderBySubmittedAtAsc(VerificationStatus.UNDER_REVIEW)
                .stream()
                .map(this::toVerificationResponse)
                .toList();
    }

    public List<AdminReportResponse> getReportedUsers() {
        return conversationReportRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toReportResponse)
                .toList();
    }

    @Transactional
    public String suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEnabled(false);
        userRepository.save(user);
        return "User suspended successfully";
    }

    @Transactional
    public String reviewVerification(Long verificationId, boolean approved, String note) {
        VerificationRecord record = verificationRepository.findById(verificationId)
                .orElseThrow(() -> new RuntimeException("Verification record not found"));

        VerificationStatus status = approved ? VerificationStatus.APPROVED : VerificationStatus.REJECTED;
        String reviewNote = (note == null || note.isBlank())
                ? (approved ? "Approved by admin" : "Rejected by admin")
                : note.trim();

        verificationRepository.updateVerificationStatus(verificationId, status, reviewNote);

        User user = record.getUser();
        boolean identityVerified = approved;
        user.setIdentityVerified(identityVerified);
        userRepository.save(user);

        profileRepository.findByUserId(user.getId()).ifPresent(profile -> {
            profile.setIdentityVerified(identityVerified);
            profileRepository.save(profile);
        });

        return approved ? "Verification approved" : "Verification rejected";
    }

    public AdminAnalyticsResponse getAnalytics() {
        AdminAnalyticsResponse response = new AdminAnalyticsResponse();
        response.setTotalUsers(userRepository.count());
        response.setActiveUsers(userRepository.countByEnabledTrue());
        response.setMatchesGenerated(matchRepository.countByAiGeneratedTrue());
        return response;
    }

    private AdminUserSummaryResponse toUserSummary(User user) {
        AdminUserSummaryResponse response = new AdminUserSummaryResponse();
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setRole(user.getRole());
        response.setEnabled(user.isEnabled());
        response.setEmailVerified(user.isEmailVerified());
        response.setIdentityVerified(user.isIdentityVerified());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    private AdminVerificationResponse toVerificationResponse(VerificationRecord record) {
        AdminVerificationResponse response = new AdminVerificationResponse();
        response.setVerificationId(record.getId());
        response.setUserId(record.getUser() != null ? record.getUser().getId() : null);
        response.setUserEmail(record.getUser() != null ? record.getUser().getEmail() : null);
        response.setStatus(record.getStatus());
        response.setReviewNote(record.getReviewNote());
        response.setSubmittedAt(record.getSubmittedAt());
        return response;
    }

    private AdminReportResponse toReportResponse(ConversationReport report) {
        AdminReportResponse response = new AdminReportResponse();
        response.setReportId(report.getId());
        response.setConversationId(report.getConversation() != null ? report.getConversation().getId() : null);
        response.setReporterUserId(report.getReporter() != null ? report.getReporter().getId() : null);
        response.setReportedUserId(report.getReportedUser() != null ? report.getReportedUser().getId() : null);
        response.setReason(report.getReason());
        response.setDetails(report.getDetails());
        response.setCreatedAt(report.getCreatedAt() != null ? report.getCreatedAt() : LocalDateTime.now());
        return response;
    }
}
