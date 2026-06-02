package com.base.repository;

import com.base.entity.GoodsReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {
    Page<GoodsReceipt> findBySupplier_SupplierId(Long supplierId, Pageable pageable);
    Page<GoodsReceipt> findByCreatedBy_UserId(Long userId, Pageable pageable);
}
