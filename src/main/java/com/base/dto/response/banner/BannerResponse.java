package com.base.dto.response.banner;

import com.base.entity.Banner;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BannerResponse {

    private Long bannerId;

    private String title;

    private String imageUrl;

    private String redirectUrl;

    private Banner.BannerPosition position;

    private Integer displayOrder;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
