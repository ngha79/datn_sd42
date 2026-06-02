package com.base.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "suppliers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "supplier_name", nullable = false)
    private String supplierName;

    private String phone;

    private String email;

    private String address;

    @Builder.Default
    private boolean status = true;
}
