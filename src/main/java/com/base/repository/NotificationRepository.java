package com.base.repository;

import com.base.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByUser_UserId(Long userId, Pageable pageable);
    Page<Notification> findByUser_UserIdAndIsRead(Long userId, boolean isRead, Pageable pageable);
    long countByUser_UserIdAndIsRead(Long userId, boolean isRead);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId")
    void markAllAsReadByUserId(Long userId);
}
