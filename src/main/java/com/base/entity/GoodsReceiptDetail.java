package com.base.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "goods_receipt_details")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class GoodsReceiptDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_detail_id")
    private Long receiptDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private GoodsReceipt receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private ProductVariant variant;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "import_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal importPrice;

    @Column(precision = 15, scale = 2, nullable = false)
    private BigDecimal subtotal;
}
