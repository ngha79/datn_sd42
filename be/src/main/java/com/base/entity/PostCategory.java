package com.base.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "post_categories")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PostCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_category_id")
    private Long postCategoryId;

    @Column(name = "category_name", nullable = false, unique = true)
    private String categoryName;
}
