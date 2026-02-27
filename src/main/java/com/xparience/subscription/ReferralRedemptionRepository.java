package com.xparience.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReferralRedemptionRepository extends JpaRepository<ReferralRedemption, Long> {
    boolean existsByRedeemedByUserId(Long redeemedByUserId);
}
