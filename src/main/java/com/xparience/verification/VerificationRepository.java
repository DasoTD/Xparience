package com.xparience.verification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VerificationRepository extends JpaRepository<VerificationRecord, Long> {

    Optional<VerificationRecord> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndStatus(Long userId, VerificationStatus status);

    @Query("SELECT v FROM VerificationRecord v WHERE v.status = 'UNDER_REVIEW' " +
           "ORDER BY v.submittedAt ASC")
    List<VerificationRecord> findAllUnderReview();

    @Modifying
    @Query("UPDATE VerificationRecord v SET v.status = :status, " +
           "v.reviewedAt = CURRENT_TIMESTAMP, v.reviewNote = :note WHERE v.id = :id")
    void updateVerificationStatus(@Param("id") Long id,
                                   @Param("status") VerificationStatus status,
                                   @Param("note") String note);
}