package com.base.controller;

import com.base.dto.request.post.PostRequest;
import com.base.dto.response.ApiResponse;
import com.base.dto.response.post.PostResponse;
import com.base.entity.Post;
import com.base.entity.PostCategory;
import com.base.entity.PostTag;
import com.base.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(
            value = "/admin/posts",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<PostResponse>> create(
            @ModelAttribute PostRequest request,
           @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.create(request, file)));
    }

    @PutMapping(
            value = "/admin/posts/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<PostResponse>> update(
            @PathVariable Long id,
            @ModelAttribute PostRequest request,
            @RequestPart("file") MultipartFile file
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.update(id, request, file)));
    }

    @GetMapping("/posts/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postService.getById(id)));
    }

    @GetMapping("/admin/posts/{id}")
    public ResponseEntity<ApiResponse<PostResponse>> adminGetById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postService.adminGetById(id)));
    }

    @GetMapping("/posts/tags")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllByTag(
            Pageable pageable,
            @RequestParam(required = false) Long tagId
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.getAllByTag(pageable, tagId)));
    }

    @GetMapping("/posts/categories")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllByCategory(
            Pageable pageable,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.getAllByCategory(pageable, categoryId)));
    }

    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllByStatus(
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.getAllUser(pageable)));
    }

    @GetMapping("/admin/posts")
    public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllByAdmin(
            Pageable pageable,
            @RequestParam(required = false)  Post.PostStatus status,
            @RequestParam(required = false) Set<PostTag> tagIds,
            @RequestParam(required = false) Set<PostCategory> categoryIds
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.getAll(status, tagIds, categoryIds, pageable)));
    }

    @DeleteMapping("/admin/posts/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id
    ) {
        postService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/posts/{id}/status")
    public ResponseEntity<ApiResponse<PostResponse>> updateByStatus(
            @PathVariable Long id,
            Post.PostStatus status
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.updateByStatus(id, status)));
    }
}
