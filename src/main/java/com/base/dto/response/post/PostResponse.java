package com.base.dto.response.post;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PostResponse {
    private Long postId;
    private String title;
    private String slug;
    private String thumbnail;
    private String summary;
    private String content;
    private String status;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String authorName;
    private Set<String> categories;
    private Set<String> tags;
}
