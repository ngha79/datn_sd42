package com.base.service.impl;

import com.base.dto.request.postTag.PostTagRequest;
import com.base.dto.response.postTag.PostTagResponse;
import com.base.entity.PostTag;
import com.base.exception.ResourceAlreadyExistsException;
import com.base.exception.ResourceNotFoundException;
import com.base.repository.PostTagRepository;
import com.base.service.PostTagService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostTagServiceImpl implements PostTagService {
    private final PostTagRepository postTagRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<PostTagResponse> getPostTags() {
        return postTagRepository.findAll().stream().map(postTag -> modelMapper.map(postTag, PostTagResponse.class)).collect(Collectors.toList());
    }

    @Override
    public PostTagResponse getPostTag(Long postTagId) {
        return postTagRepository
                .findById(postTagId)
                .stream()
                .map(postTag -> modelMapper.map(postTag, PostTagResponse.class))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục bài viết không tồn tại!"));
    }

    @Override
    public PostTagResponse createPostTag(PostTagRequest postTagRequest) {
        if(postTagRepository.existsByTagName(postTagRequest.getTagName().trim())) {
            throw new ResourceAlreadyExistsException("Tên danh mục bài viết đã tồn tại!");
        }

        PostTag postTag = modelMapper.map(postTagRequest, PostTag.class);

        postTag = postTagRepository.save(postTag);

        return modelMapper.map(postTag, PostTagResponse.class);
    }

    @Override
    public PostTagResponse updatePostTag(Long postTagId, PostTagRequest request) {

        PostTag postTag = postTagRepository.findById(postTagId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        if (postTagRepository.existsByTagNameAndTagIdNot(
                request.getTagName(), postTagId)) {
            throw new ResourceAlreadyExistsException("Tên danh mục bài viết đã tồn tại!");
        }

        modelMapper.map(request, postTag);

        PostTag updated = postTagRepository.save(postTag);

        return modelMapper.map(updated, PostTagResponse.class);
    }

    @Override
    public void deletePostTag(Long postTagId) {
        postTagRepository.deleteById(postTagId);
    }
}
