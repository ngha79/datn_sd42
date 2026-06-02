package com.base.repository;

import com.base.entity.Supplier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Optional<Supplier> findByPhone(String phone);
    Optional<Supplier> findByEmail(String email);
    Page<Supplier> findByStatus(boolean status, Pageable pageable);

    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
