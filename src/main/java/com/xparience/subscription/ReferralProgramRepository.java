package com.xparience.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReferralProgramRepository extends JpaRepository<ReferralProgram, Long> {
    Optional<ReferralProgram> findByUserId(Long userId);
    Optional<ReferralProgram> findByReferralCode(String referralCode);
}
