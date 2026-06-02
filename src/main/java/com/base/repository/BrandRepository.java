package com.base.repository;

import com.base.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByBrandName(String brandName);
    boolean existsByBrandName(String brandName);
    boolean existsByBrandNameAndBrandIdNot(String brandName, Long id);
}
