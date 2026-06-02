package com.base.repository;

import com.base.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCode(String code);
    boolean existsByCode(String code);

    @Query("""
        SELECT c FROM Coupon c
        WHERE c.code = :code
          AND c.status = true
          AND c.quantity > 0
          AND c.startDate <= :now
          AND c.endDate >= :now
    """)
    Optional<Coupon> findValidCoupon(String code, LocalDateTime now);

    List<Coupon> findByStatusAndEndDateAfter(boolean status, LocalDateTime date);
}
