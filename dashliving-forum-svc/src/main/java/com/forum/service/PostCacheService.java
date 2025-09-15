package com.forum.service;

import com.forum.common.dto.response.PostResponse;
import com.forum.common.exception.ResourceNotFoundException;
import com.forum.common.model.Post;
import com.forum.common.model.PostWithUserName;
import com.forum.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCacheService {

    private final PostRepository postRepository;

    @Cacheable(
            value = "hotPosts",
            key = "#postId",
            unless = "#result == null or #result.viewCount <= 10"
    )
    @Transactional(readOnly = true)
    public PostResponse getPostByIdFromCacheOrDB(Long postId) {
        PostWithUserName postWithUserName = postRepository.findPostWithUsernameById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        return mapToPostResponse(postWithUserName.getPost(), postWithUserName.getUsername());
    }

    private PostResponse mapToPostResponse(Post post, String username) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setAuthorId(post.getAuthorId());
        response.setAuthorUsername(username);
        response.setCreatedAt(post.getCreatedAt());
        response.setUpdatedAt(post.getUpdatedAt());
        response.setViewCount(post.getViewCount());
        response.setPublished(post.isPublished());
        return response;
    }
}