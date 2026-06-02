package com.base.repository;

import com.base.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveOrderByCreatedAtDesc(boolean isActive);
    List<Banner> findByPositionAndIsActive(String position, boolean isActive);
}
