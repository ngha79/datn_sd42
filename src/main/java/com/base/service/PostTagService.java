package com.base.service;

import com.base.dto.request.postTag.PostTagRequest;
import com.base.dto.response.postTag.PostTagResponse;

import java.util.List;

public interface PostTagService {
    List<PostTagResponse> getPostTags();

    PostTagResponse getPostTag(Long postTagId);

    PostTagResponse createPostTag(PostTagRequest postTagRequest);

    PostTagResponse updatePostTag(Long postTagId, PostTagRequest postTagRequest);

    void deletePostTag(Long postTagId);
}
