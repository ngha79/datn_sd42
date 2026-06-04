package com.base.controller;

import com.base.dto.request.postCategory.PostCategoryRequest;
import com.base.dto.response.ApiResponse;
import com.base.dto.response.postCategory.PostCategoryResponse;
import com.base.service.PostCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostCategoryController {
    private final PostCategoryService postCategoryService;

    @GetMapping("/post-category")
    public ResponseEntity<ApiResponse<List<PostCategoryResponse>>> getAllPostCategory() {
        return ResponseEntity.ok(ApiResponse.success(postCategoryService.getPostCategories()));
    }

    @GetMapping("/post-category/{id}")
    public ResponseEntity<ApiResponse<PostCategoryResponse>> getPostCategory(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postCategoryService.getPostCategory(id)));
    }

    @PostMapping("/admin/post-category")
    public ResponseEntity<ApiResponse<PostCategoryResponse>> createPostCategory(@RequestBody PostCategoryRequest postCategoryRequest) {
        return ResponseEntity.ok(ApiResponse.success(postCategoryService.createPostCategory(postCategoryRequest)));
    }

    @PutMapping("/admin/post-category/{id}")
    public ResponseEntity<ApiResponse<PostCategoryResponse>> updatePostCategory(@PathVariable Long id, @RequestBody PostCategoryRequest postCategoryRequest) {
        return ResponseEntity.ok(ApiResponse.success(postCategoryService.updatePostCategory(id, postCategoryRequest)));
    }

    @DeleteMapping("/admin/post-category/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePostCategory(@PathVariable Long id) {
        postCategoryService.deletePostCategory(id);
        return ResponseEntity.noContent().build();
    }
}
