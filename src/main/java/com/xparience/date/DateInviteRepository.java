package com.xparience.date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DateInviteRepository extends JpaRepository<DateInvite, Long> {

    @Query("SELECT d FROM DateInvite d WHERE " +
           "d.sender.id = :userId OR d.recipient.id = :userId " +
           "ORDER BY d.createdAt DESC")
    List<DateInvite> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT d FROM DateInvite d WHERE " +
           "(d.sender.id = :u1 AND d.recipient.id = :u2) OR " +
           "(d.sender.id = :u2 AND d.recipient.id = :u1) " +
           "ORDER BY d.createdAt DESC")
    List<DateInvite> findDateHistory(@Param("u1") Long u1, @Param("u2") Long u2);

    List<DateInvite> findByRecipientIdAndStatus(Long recipientId, DateStatus status);

    @Query("SELECT d FROM DateInvite d WHERE " +
           "(d.sender.id = :userId OR d.recipient.id = :userId) " +
           "AND d.status = 'ACCEPTED' AND d.scheduledAt >= :now " +
           "ORDER BY d.scheduledAt ASC")
    List<DateInvite> findUpcomingDates(@Param("userId") Long userId,
                                       @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE DateInvite d SET d.status = :status, " +
           "d.respondedAt = CURRENT_TIMESTAMP WHERE d.id = :id")
    void updateStatus(@Param("id") Long id, @Param("status") DateStatus status);
}