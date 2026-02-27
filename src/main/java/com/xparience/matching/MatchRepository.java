package com.xparience.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    @Query("SELECT m FROM Match m WHERE " +
           "(m.userOne.id = :userId OR m.userTwo.id = :userId) " +
           "AND m.status = :status ORDER BY m.createdAt DESC")
    List<Match> findByUserIdAndStatus(@Param("userId") Long userId,
                                      @Param("status") MatchStatus status);

    @Query("SELECT m FROM Match m WHERE " +
           "(m.userOne.id = :userId OR m.userTwo.id = :userId) " +
           "AND m.aiGenerated = true " +
           "AND m.status = 'PENDING' " +
           "AND (m.expiresAt IS NULL OR m.expiresAt > :now) " +
           "ORDER BY m.overallCompatibilityScore DESC")
    List<Match> findPendingAiMatchesByUserId(@Param("userId") Long userId,
                                             @Param("now") LocalDateTime now);

    @Query("SELECT m FROM Match m WHERE " +
           "(m.userOne.id = :userOneId AND m.userTwo.id = :userTwoId) OR " +
           "(m.userOne.id = :userTwoId AND m.userTwo.id = :userOneId)")
    Optional<Match> findByUserIds(@Param("userOneId") Long userOneId,
                                  @Param("userTwoId") Long userTwoId);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM Match m WHERE " +
           "((m.userOne.id = :userOneId AND m.userTwo.id = :userTwoId) OR " +
           "(m.userOne.id = :userTwoId AND m.userTwo.id = :userOneId)) " +
           "AND m.status = 'ACCEPTED'")
    boolean areUsersMatched(@Param("userOneId") Long userOneId,
                            @Param("userTwoId") Long userTwoId);

    @Modifying
    @Query("UPDATE Match m SET m.status = :status WHERE m.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") MatchStatus status);

       @Modifying
       @Query("UPDATE Match m SET m.status = 'EXPIRED' WHERE m.status = 'PENDING' AND m.expiresAt <= :now")
       int expirePendingMatches(@Param("now") LocalDateTime now);

       @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM Match m WHERE " +
                 "((m.userOne.id = :userOneId AND m.userTwo.id = :userTwoId) OR " +
                 "(m.userOne.id = :userTwoId AND m.userTwo.id = :userOneId)) " +
                 "AND m.createdAt >= :cutoff")
       boolean existsMatchBetweenUsersSince(@Param("userOneId") Long userOneId,
                                                                       @Param("userTwoId") Long userTwoId,
                                                                       @Param("cutoff") LocalDateTime cutoff);

       @Query("SELECT CASE WHEN COUNT(m) > 0 THEN TRUE ELSE FALSE END FROM Match m WHERE " +
              "((m.userOne.id = :userOneId AND m.userTwo.id = :userTwoId) OR " +
              "(m.userOne.id = :userTwoId AND m.userTwo.id = :userOneId)) " +
              "AND m.createdAt >= :cutoff AND m.id <> :excludedMatchId")
       boolean existsOtherMatchBetweenUsersSince(@Param("userOneId") Long userOneId,
                                                 @Param("userTwoId") Long userTwoId,
                                                 @Param("cutoff") LocalDateTime cutoff,
                                                 @Param("excludedMatchId") Long excludedMatchId);

       @Query("SELECT COUNT(m) FROM Match m WHERE (m.userOne.id = :userId OR m.userTwo.id = :userId) " +
              "AND m.aiGenerated = true AND m.createdAt >= :from AND m.createdAt < :to")
       long countAiMatchesForUserBetween(@Param("userId") Long userId,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);

       long countByAiGeneratedTrue();
}