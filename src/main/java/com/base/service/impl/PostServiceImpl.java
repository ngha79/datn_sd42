package com.base.service.impl;


import com.base.dto.request.ImageUploadBannerMessage;
import com.base.dto.request.ImageUploadPostMessage;
import com.base.dto.request.post.PostRequest;
import com.base.dto.response.post.PostResponse;
import com.base.entity.*;
import com.base.exception.BadRequestException;
import com.base.exception.ResourceNotFoundException;
import com.base.queue.ImageUploadProducer;
import com.base.repository.*;
import com.base.service.PostService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LocalStorageService localStorageService;
    private final ImageUploadProducer imageUploadProducer;
    private final PostCategoryRepository categoryRepository;
    private final PostTagRepository tagRepository;
    private final ModelMapper modelMapper;

    @Override
    public PostResponse create(PostRequest request, MultipartFile file) {

        User author = userRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));

        Set<PostCategory> categories = new HashSet<>(
                categoryRepository.findAllById(request.getCategoryIds())
        );

        Set<PostTag> tags = new HashSet<>(
                tagRepository.findAllById(request.getTagIds())
        );

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ảnh banner");
        }

        String tempPath = localStorageService.saveTempFile(file);
        String tempUrl = localStorageService.getTempUrl(tempPath);

        Post post = Post.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .summary(request.getSummary())
                .content(request.getContent())
                .author(author)
                .categories(categories)
                .tags(tags)
                .thumbnail(tempUrl)
                .status(Post.PostStatus.DRAFT)
                .build();

        post = postRepository.save(post);

        imageUploadProducer.sendUploadPostMessage(
                ImageUploadPostMessage.builder()
                        .postId(post.getPostId())
                        .tempFilePath(tempPath)
                        .action(ImageUploadPostMessage.ActionType.CREATE)
                        .build()
        );

        return toResponse(post);
    }

    @Override
    public PostResponse update(Long id, PostRequest request, MultipartFile file) {

        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());

        if (request.getCategoryIds() != null) {
            post.setCategories(
                    new HashSet<>(categoryRepository.findAllById(request.getCategoryIds()))
            );
        }

        if (request.getTagIds() != null) {
            post.setTags(
                    new HashSet<>(tagRepository.findAllById(request.getTagIds()))
            );
        }

        if (file != null && !file.isEmpty()) {

            String tempPath = localStorageService.saveTempFile(file);
            String tempUrl = localStorageService.getTempUrl(tempPath);

            post.setThumbnail(tempUrl);

            imageUploadProducer.sendUploadPostMessage(
                    ImageUploadPostMessage.builder()
                            .postId(post.getPostId())
                            .tempFilePath(tempPath)
                            .action(ImageUploadPostMessage.ActionType.UPDATE)
                            .build()
            );
        }

        post = postRepository.save(post);

        return toResponse(post);
    }

    @Transactional
    @Override
    public PostResponse getById(Long id) {
        Post post = postRepository.findByIdAndPublished(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        postRepository.save(post);
        incrementViewCount(id);
        return toResponse(post);
    }

    @Override
    public PostResponse adminGetById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        postRepository.save(post);
        return toResponse(post);
    }

    @Override
    public Page<PostResponse> getAllByTag(Pageable pageable, Long tagId) {
        return postRepository
                .findByTagIdAndPublished(tagId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<PostResponse> getAllByCategory(Pageable pageable, Long categoryId) {
        return postRepository
                .findByCategoryIdAndPublished(categoryId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<PostResponse> getAllUser(Pageable pageable) {
        return postRepository
                .findByStatus(Post.PostStatus.PUBLISHED, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<PostResponse> getAll(
            Post.PostStatus status,
            Set<PostTag> tagIds,
            Set<PostCategory> categoryIds,
            Pageable pageable) {

        Specification<Post> spec = Specification.where(null);

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.join("tags").in(tagIds));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    root.join("categories").in(categoryIds));
        }

        return postRepository.findAll(spec, pageable)
                .map(this::toResponse);
    }

    @Override
    public void delete(Long id) {
        postRepository.deleteById(id);
    }

    @Override
    public PostResponse updateByStatus(Long id, Post.PostStatus status) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        post.setStatus(status);

        return toResponse(postRepository.save(post));
    }

    @Override
    public void incrementViewCount(Long id) {
        postRepository.incrementViewCount(id);
    }

    private PostResponse toResponse(Post post) {
        PostResponse response = modelMapper.map(post, PostResponse.class);

        response.setAuthorName(post.getAuthor().getUsername());

        response.setCategories(
                post.getCategories().stream()
                        .map(PostCategory::getCategoryName)
                        .collect(Collectors.toSet())
        );

        response.setTags(
                post.getTags().stream()
                        .map(PostTag::getTagName)
                        .collect(Collectors.toSet())
        );

        return response;
    }
}
