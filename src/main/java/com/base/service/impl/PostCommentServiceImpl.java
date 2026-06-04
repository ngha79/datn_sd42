package com.base.service.impl;

import com.base.dto.request.comment.CommentRequest;
import com.base.dto.response.comment.CommentResponse;
import com.base.entity.Post;
import com.base.entity.PostComment;
import com.base.entity.User;
import com.base.exception.BadRequestException;
import com.base.exception.ForbiddenException;
import com.base.exception.ResourceNotFoundException;
import com.base.repository.PostCommentRepository;
import com.base.repository.PostRepository;
import com.base.repository.UserRepository;
import com.base.service.PostCommentService;
import com.base.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsByPost(Long postId, Pageable pageable) {

        postRepository.findByPostIdAndStatus(postId, Post.PostStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));

        return commentRepository
                .findByPost_PostIdAndParentIsNullAndDeletedFalse(postId, pageable)
                .map(this::toResponse);
    }

    @Override
    public CommentResponse addComment(Long postId, CommentRequest request) {

        Long userId = securityUtils.getCurrentUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = postRepository.findByPostIdAndStatus(postId, Post.PostStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Bài viết không tồn tại"));

        PostComment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comment không tồn tại"));

            if (parent.getParent() != null) {
                throw new BadRequestException("Không thể reply vào một reply");
            }

            if (!parent.getPost().getPostId().equals(postId)) {
                throw new BadRequestException("Comment không thuộc bài viết này");
            }
        }

        PostComment comment = PostComment.builder()
                .post(post)
                .user(user)
                .parent(parent)
                .content(request.getContent())
                .build();

        return toResponse(commentRepository.save(comment));
    }

    @Override
    public CommentResponse updateComment(Long commentId, CommentRequest request) {

        Long userId = securityUtils.getCurrentUserId();

        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment không tồn tại"));

        if (comment.isDeleted()) {
            throw new BadRequestException("Comment đã bị xóa");
        }

        if (!comment.isOwner(userId)) {
            throw new ForbiddenException("Bạn không có quyền sửa comment này");
        }

        comment.setContent(request.getContent());

        return toResponse(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long commentId) {

        Long userId = securityUtils.getCurrentUserId();

        PostComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment không tồn tại"));

        boolean isOwner = comment.isOwner(userId);
        boolean isPostAuthor = comment.getPost().getAuthor().getUserId().equals(userId);

        if (!isOwner && !isPostAuthor) {
            throw new ForbiddenException("Bạn không có quyền xóa comment này");
        }

        comment.setDeleted(true);

        commentRepository.save(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public long countComments(Long postId) {
        return commentRepository.countByPost_PostIdAndDeletedFalse(postId);
    }

    private CommentResponse toResponse(PostComment comment) {
        List<CommentResponse> replies = comment.getReplies() == null
                ? List.of()
                : comment.getReplies().stream()
                .filter(r -> !r.isDeleted())
                .map(this::toResponse)
                .toList();

        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPost().getPostId())
                .userId(comment.getUser().getUserId())
                .username(comment.getUser().getUsername())
                .userAvatar(comment.getUser().getAvatar())
                .content(comment.isDeleted() ? "[Bình luận đã bị xóa]" : comment.getContent())
                .parentId(comment.getParent() != null ? comment.getParent().getCommentId() : null)
                .replies(replies)
                .deleted(comment.isDeleted())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}
