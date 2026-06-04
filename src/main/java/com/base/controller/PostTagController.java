package com.base.controller;

import com.base.dto.request.postTag.PostTagRequest;
import com.base.dto.response.ApiResponse;
import com.base.dto.response.postTag.PostTagResponse;
import com.base.service.PostTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PostTagController {
    private final PostTagService postTagService;

    @GetMapping("/post-tag")
    public ResponseEntity<ApiResponse<List<PostTagResponse>>> getAllPostTag() {
        return ResponseEntity.ok(ApiResponse.success(postTagService.getPostTags()));
    }

    @GetMapping("/post-tag/{id}")
    public ResponseEntity<ApiResponse<PostTagResponse>> getPostTag(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postTagService.getPostTag(id)));
    }

    @PostMapping("/admin/post-tag")
    public ResponseEntity<ApiResponse<PostTagResponse>> createPostTag(@RequestBody PostTagRequest postTagRequest) {
        return ResponseEntity.ok(ApiResponse.success(postTagService.createPostTag(postTagRequest)));
    }

    @PutMapping("/admin/post-tag/{id}")
    public ResponseEntity<ApiResponse<PostTagResponse>> updatePostTag(@PathVariable Long id, @RequestBody PostTagRequest postTagRequest) {
        return ResponseEntity.ok(ApiResponse.success(postTagService.updatePostTag(id, postTagRequest)));
    }

    @DeleteMapping("/admin/post-tag/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePostTag(@PathVariable Long id) {
        postTagService.deletePostTag(id);
        return ResponseEntity.noContent().build();
    }
}
