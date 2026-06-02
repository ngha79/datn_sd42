package com.base.repository;

import com.base.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Page<Conversation> findByUser_UserId(Long userId, Pageable pageable);
    Page<Conversation> findByStaff_UserIdAndStatus(Long staffId, Conversation.ConversationStatus status, Pageable pageable);
    Page<Conversation> findByStatus(Conversation.ConversationStatus status, Pageable pageable);
    Optional<Conversation> findByUser_UserIdAndStatus(Long userId, Conversation.ConversationStatus status);
}
