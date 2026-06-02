package com.base.repository;

import com.base.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findByConversation_ConversationIdOrderBySentAtAsc(Long conversationId, Pageable pageable);

    long countByConversation_ConversationIdAndIsReadFalseAndSender_UserIdNot(Long conversationId, Long userId);

    @Modifying
    @Query("UPDATE Message m SET m.isRead = true WHERE m.conversation.conversationId = :conversationId AND m.sender.userId <> :userId")
    void markAllAsRead(Long conversationId, Long userId);
}
