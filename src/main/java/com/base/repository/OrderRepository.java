package com.base.repository;

import com.base.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByUser_UserId(Long userId, Pageable pageable);

    Page<Order> findByUser_UserIdAndOrderStatus(Long userId, Order.OrderStatus status, Pageable pageable);

    @Query("""
        SELECT o FROM Order o
        WHERE (:status IS NULL OR o.orderStatus = :status)
          AND (:from IS NULL OR o.orderDate >= :from)
          AND (:to IS NULL OR o.orderDate <= :to)
    """)
    Page<Order> findAllWithFilters(
        @Param("status") Order.OrderStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        Pageable pageable
    );

    @Query("SELECT SUM(o.finalAmount) FROM Order o WHERE o.orderStatus = 'DELIVERED' AND o.orderDate BETWEEN :from AND :to")
    java.math.BigDecimal sumRevenue(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
