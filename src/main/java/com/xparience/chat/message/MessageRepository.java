package com.xparience.chat.message;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByConversationIdOrderBySentAtAsc(Long conversationId);

       Optional<Message> findFirstByConversationIdOrderBySentAtAsc(Long conversationId);

    @Query("SELECT COUNT(m) FROM Message m WHERE " +
           "m.conversation.id = :conversationId " +
           "AND m.sender.id != :userId AND m.isRead = false")
    int countUnreadMessages(@Param("conversationId") Long conversationId,
                            @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.isDelivered = true, m.deliveredAt = CURRENT_TIMESTAMP " +
           "WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id != :userId AND m.isDelivered = false")
    void markAllAsDelivered(@Param("conversationId") Long conversationId,
                            @Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true, m.readAt = CURRENT_TIMESTAMP " +
           "WHERE m.conversation.id = :conversationId " +
           "AND m.sender.id != :userId AND m.isRead = false")
    void markAllAsRead(@Param("conversationId") Long conversationId,
                       @Param("userId") Long userId);
}