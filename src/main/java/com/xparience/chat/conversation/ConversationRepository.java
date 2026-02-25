package com.xparience.chat.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c WHERE " +
           "c.participantOne.id = :userId OR c.participantTwo.id = :userId " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST")
    List<Conversation> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.participantOne.id = :p1 AND c.participantTwo.id = :p2) OR " +
           "(c.participantOne.id = :p2 AND c.participantTwo.id = :p1)")
    Optional<Conversation> findByParticipants(@Param("p1") Long p1,
                                              @Param("p2") Long p2);

    @Modifying
    @Query("UPDATE Conversation c SET c.lastMessage = :msg, " +
           "c.lastMessageAt = CURRENT_TIMESTAMP WHERE c.id = :id")
    void updateLastMessage(@Param("id") Long id, @Param("msg") String msg);
}