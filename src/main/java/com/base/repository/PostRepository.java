package com.base.repository;

import com.base.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlug(String slug);
    boolean existsBySlug(String slug);

    Page<Post> findByStatus(Post.PostStatus status, Pageable pageable);

    Page<Post> findByAuthor_UserIdAndStatus(Long authorId, Post.PostStatus status, Pageable pageable);

    @Query("""
        SELECT p FROM Post p JOIN p.categories c
        WHERE c.postCategoryId = :categoryId AND p.status = 'PUBLISHED'
    """)
    Page<Post> findByCategoryIdAndPublished(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("""
        SELECT p FROM Post p JOIN p.tags t
        WHERE t.tagId = :tagId AND p.status = 'PUBLISHED'
    """)
    Page<Post> findByTagIdAndPublished(@Param("tagId") Long tagId, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.postId = :postId")
    void incrementViewCount(@Param("postId") Long postId);
}
