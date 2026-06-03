package com.base.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "consignee_name")
    private String consigneeName;

    private String phone;

    private String province;

    private String district;

    private String ward;

    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "is_default")
    @Builder.Default
    private Boolean isDefault = false;
}
