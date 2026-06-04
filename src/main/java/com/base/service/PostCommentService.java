package com.base.service;

import com.base.dto.request.comment.CommentRequest;
import com.base.dto.response.comment.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostCommentService {
    Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable);
    CommentResponse addComment(Long postId, CommentRequest request);
    CommentResponse updateComment(Long commentId, CommentRequest request);
    void deleteComment(Long commentId);
    long countComments(Long postId);
}
