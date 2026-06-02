package com.base.repository;

import com.base.entity.PosShift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PosShiftRepository extends JpaRepository<PosShift, Long> {
    Page<PosShift> findByStaff_UserId(Long staffId, Pageable pageable);

    /** Tìm ca đang mở (chưa có end_time) của nhân viên */
    @Query("SELECT s FROM PosShift s WHERE s.staff.userId = :staffId AND s.endTime IS NULL")
    Optional<PosShift> findOpenShiftByStaffId(Long staffId);
}
