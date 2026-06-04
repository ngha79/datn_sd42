package com.base.dto.request.post;

import lombok.Data;

import java.util.Set;

@Data
public class PostRequest {
    private String title;
    private String slug;
    private String thumbnail;
    private String summary;
    private String content;
    private Set<Long> categoryIds;
    private Set<Long> tagIds;
    private Long authorId;
}
