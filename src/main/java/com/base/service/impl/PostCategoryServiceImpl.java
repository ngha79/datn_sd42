package com.base.service.impl;

import com.base.dto.request.postCategory.PostCategoryRequest;
import com.base.dto.response.postCategory.PostCategoryResponse;
import com.base.entity.PostCategory;
import com.base.exception.ResourceAlreadyExistsException;
import com.base.exception.ResourceNotFoundException;
import com.base.repository.PostCategoryRepository;
import com.base.service.PostCategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostCategoryServiceImpl  implements PostCategoryService {
    private final PostCategoryRepository postCategoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<PostCategoryResponse> getPostCategories() {
        return postCategoryRepository.findAll().stream().map(postCategory -> modelMapper.map(postCategory, PostCategoryResponse.class)).collect(Collectors.toList());
    }

    @Override
    public PostCategoryResponse getPostCategory(Long postCategoryId) {
        return postCategoryRepository
                .findById(postCategoryId)
                .stream()
                .map(postCategory -> modelMapper.map(postCategory, PostCategoryResponse.class))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục bài viết không tồn tại!"));
    }

    @Override
    public PostCategoryResponse createPostCategory(PostCategoryRequest postCategoryRequest) {
        if(postCategoryRepository.existsByCategoryName(postCategoryRequest.getPostCategoryName().trim())) {
            throw new ResourceAlreadyExistsException("Tên danh mục bài viết đã tồn tại!");
        }

        PostCategory postCategory = modelMapper.map(postCategoryRequest, PostCategory.class);

        postCategory = postCategoryRepository.save(postCategory);

        return modelMapper.map(postCategory, PostCategoryResponse.class);
    }

    @Override
    public PostCategoryResponse updatePostCategory(Long postCategoryId, PostCategoryRequest request) {

        PostCategory postCategory = postCategoryRepository.findById(postCategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục"));

        if (postCategoryRepository.existsByCategoryNameAndPostCategoryIdNot(
                request.getPostCategoryName(), postCategoryId)) {
            throw new ResourceAlreadyExistsException("Tên danh mục bài viết đã tồn tại!");
        }

        modelMapper.map(request, postCategory);

        PostCategory updated = postCategoryRepository.save(postCategory);

        return modelMapper.map(updated, PostCategoryResponse.class);
    }

    @Override
    public void deletePostCategory(Long postCategoryId) {
        postCategoryRepository.deleteById(postCategoryId);
    }
}
