package com.base.service;

import com.base.dto.request.postCategory.PostCategoryRequest;
import com.base.dto.response.postCategory.PostCategoryResponse;

import java.util.List;

public interface PostCategoryService {
    List<PostCategoryResponse> getPostCategories();

    PostCategoryResponse getPostCategory(Long postCategoryId);

    PostCategoryResponse createPostCategory(PostCategoryRequest postCategoryRequest);

    PostCategoryResponse updatePostCategory(Long postCategoryId, PostCategoryRequest postCategoryRequest);

    void deletePostCategory(Long postCategoryId);
}
