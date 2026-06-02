package com.base.repository;

import com.base.entity.GoodsReceiptDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoodsReceiptDetailRepository extends JpaRepository<GoodsReceiptDetail, Long> {
    List<GoodsReceiptDetail> findByReceipt_ReceiptId(Long receiptId);
}
