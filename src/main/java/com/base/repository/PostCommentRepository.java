package com.base.repository;

import com.base.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    Page<PostComment> findByPost_PostId(Long postId, Pageable pageable);
    void deleteByPost_PostId(Long postId);
}
