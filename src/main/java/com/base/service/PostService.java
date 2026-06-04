package com.base.service;



import com.base.dto.request.post.PostRequest;
import com.base.dto.response.post.PostResponse;
import com.base.entity.Post;
import com.base.entity.PostCategory;
import com.base.entity.PostTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;


public interface PostService {

    PostResponse create(PostRequest request, MultipartFile file);

    PostResponse update(Long id, PostRequest request, MultipartFile file);

    PostResponse getById(Long id);

    PostResponse adminGetById(Long id);

    Page<PostResponse> getAllByTag(Pageable pageable, Long tagId);

    Page<PostResponse> getAllByCategory(Pageable pageable, Long categoryId);

    Page<PostResponse> getAllUser(Pageable pageable);

    Page<PostResponse> getAll(Post.PostStatus status, Set<PostTag> tagIds, Set<PostCategory> categoryIds, Pageable pageable);

    void delete(Long id);

    PostResponse updateByStatus(Long id, Post.PostStatus status);

    void incrementViewCount(Long id);
}
