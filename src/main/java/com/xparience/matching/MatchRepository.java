package com.xparience.matching;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
           "ORDER BY m.overallCompatibilityScore DESC " +
           "LIMIT 3")
    List<Match> findTop3AiMatchesByUserId(@Param("userId") Long userId);

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
}