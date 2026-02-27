package com.xparience.otp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findByTokenAndType(String token, OtpType type);

    Optional<OtpToken> findTopByUserIdAndTypeOrderByCreatedAtDesc(Long userId, OtpType type);

    Optional<OtpToken> findByUserIdAndTokenAndType(Long userId, String token, OtpType type);

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.user.id = :userId AND o.type = :type")
    void deleteAllByUserIdAndType(@Param("userId") Long userId,
                                  @Param("type") OtpType type);

    @Modifying
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now")
    void deleteAllExpired(@Param("now") LocalDateTime now);
}