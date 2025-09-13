package com.forum.service;

import com.forum.dto.request.PostRequest;
import com.forum.dto.response.PostResponse;
import com.forum.exception.ResourceNotFoundException;
import com.forum.model.Post;
import com.forum.model.PostWithUserName;
import com.forum.model.User;
import com.forum.repository.CommentRepository;
import com.forum.repository.PostRepository;
import com.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService{

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final PostCacheService postCacheService;

    @Transactional
    @CacheEvict(value = {"hotPosts"}, allEntries = true)
    public PostResponse createPost(PostRequest postRequest, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setAuthorId(userId);
        post.setPublished(true);
        post.setViewCount(0);

        Post savedPost = postRepository.save(post);

        return mapToPostResponse(savedPost, user.getUsername());
    }

    @Transactional
    public PostResponse getPostById(Long postId) {
        incrementViewCount(postId);
        return postCacheService.getPostByIdFromCacheOrDB(postId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementViewCount(Long postId) {
        int updated = postRepository.incrementViewCount(postId);
        if (updated == 0) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPublishedPosts(Pageable pageable) {
        Page<PostWithUserName> postWithUserNames = postRepository.findAllPublishedPostsWithAuthors(pageable);
        return postWithUserNames.map(p ->
                mapToPostResponse(p.getPost(), p.getUsername()));
    }

    @Transactional
    @CacheEvict(value = {"hotPosts"}, key = "#postId")
    public PostResponse updatePost(Long postId, PostRequest postRequest, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.getAuthorId().equals(userId)) {
            throw new SecurityException("You don't have permission to update this post");
        }

        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        Post updatedPost = postRepository.save(post);

        String username = userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("Unknown");

        return mapToPostResponse(updatedPost, username);
    }

    @Transactional
    @CacheEvict(value = {"hotPosts", "comment:post"}, key = "#postId")
    public void deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        if (!post.getAuthorId().equals(userId)) {
            throw new SecurityException("You don't have permission to delete this post");
        }

        commentRepository.deleteByPostId(postId);
        postRepository.delete(post);
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

    private PostResponse mapToPostResponse(Object[] result) {
        Post post = (Post) result[0];
        String username = (String) result[1];
        return mapToPostResponse(post, username);
    }
}
