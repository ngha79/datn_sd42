package com.base.repository;

import com.base.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    Page<Banner> findByIsActiveOrderByCreatedAtDesc(boolean isActive, Pageable pageable);
    Page<Banner> findByPositionAndIsActive(Banner.BannerPosition position, Boolean isActive, Pageable pageable);

}
