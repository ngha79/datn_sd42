package com.base.dto.response.comment;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentResponse {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String username;
    private String userAvatar;
    private String content;
    private Long parentId;
    private List<CommentResponse> replies;
    private boolean deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
