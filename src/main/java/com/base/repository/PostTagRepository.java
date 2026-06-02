package com.base.repository;

import com.base.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    Optional<PostTag> findByTagName(String tagName);
    boolean existsByTagName(String tagName);
}
