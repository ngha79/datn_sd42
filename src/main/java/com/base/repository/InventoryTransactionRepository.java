package com.base.repository;

import com.base.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    Page<InventoryTransaction> findByVariant_VariantId(Long variantId, Pageable pageable);
    Page<InventoryTransaction> findByType(InventoryTransaction.TransactionType type, Pageable pageable);
}
