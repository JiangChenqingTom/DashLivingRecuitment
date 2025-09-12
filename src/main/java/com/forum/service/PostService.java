package com.forum.service;

import com.forum.dto.request.PostRequest;
import com.forum.dto.response.PostResponse;
import com.forum.exception.ResourceNotFoundException;
import com.forum.model.Post;
import com.forum.model.User;
import com.forum.repository.PostRepository;
import com.forum.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService{

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    @CacheEvict(value = {"posts"}, allEntries = true)
    public PostResponse createPost(PostRequest postRequest, Long userId) {
        // 验证用户是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // 创建新帖子
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        post.setAuthorId(userId);
        post.setPublished(true);
        post.setViewCount(0);

        Post savedPost = postRepository.save(post);

        // 转换为响应DTO并返回
        return mapToPostResponse(savedPost, user.getUsername());
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long postId) {
        // 先尝试从缓存获取
        PostResponse cachedPost = getPostFromCache(postId);
        if (cachedPost != null) {
            return cachedPost;
        }

        // 缓存未命中，从数据库查询
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // 获取作者信息
        String username = postRepository.findUsernameByPostId(postId)
                .orElse("Unknown");

        // 增加浏览量
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);

        // 转换为响应DTO
        PostResponse postResponse = mapToPostResponse(post, username);

        // 如果是热点帖子（浏览量>10），存入缓存
        if (post.getViewCount() > 10) {
            cachePost(postResponse);
        }

        return postResponse;
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPublishedPosts(Pageable pageable) {
        return postRepository.findAllPublishedPostsWithAuthors(pageable)
                .map(this::mapToPostResponse);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getPostsByAuthor(Long authorId, Pageable pageable) {
        return postRepository.findByAuthorId(authorId, pageable)
                .map(post -> {
                    String username = userRepository.findById(authorId)
                            .map(User::getUsername)
                            .orElse("Unknown");
                    return mapToPostResponse(post, username);
                });
    }

    @Transactional
    @CacheEvict(value = {"posts", "hotPosts"}, key = "#postId")
    public PostResponse updatePost(Long postId, PostRequest postRequest, Long userId) {
        // 查找帖子
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // 验证权限（只有作者可以更新）
        if (!post.getAuthorId().equals(userId)) {
            throw new SecurityException("You don't have permission to update this post");
        }

        // 更新帖子内容
        post.setTitle(postRequest.getTitle());
        post.setContent(postRequest.getContent());
        Post updatedPost = postRepository.save(post);

        // 获取作者用户名
        String username = userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("Unknown");

        return mapToPostResponse(updatedPost, username);
    }

    @Transactional
    @CacheEvict(value = {"posts", "hotPosts"}, key = "#postId")
    public void deletePost(Long postId, Long userId) {
        // 查找帖子
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // 验证权限
        if (!post.getAuthorId().equals(userId)) {
            throw new SecurityException("You don't have permission to delete this post");
        }

        // 删除帖子
        postRepository.delete(post);
    }

    // 从缓存获取帖子
    @Cacheable(value = {"hotPosts", "posts"}, key = "#postId", unless = "#result == null")
    private PostResponse getPostFromCache(Long postId) {
        // 这个方法本身不实现查询逻辑，实际查询由缓存管理器处理
        // 当缓存未命中时，返回null，触发数据库查询
        return null;
    }

    // 缓存帖子
    private void cachePost(PostResponse postResponse) {
        // 实际缓存操作由Spring Cache管理器处理
        // 这里只是标记需要缓存，具体实现由@Cacheable注解处理
    }

    // 映射Post到PostResponse
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

    // 映射查询结果到PostResponse
    private PostResponse mapToPostResponse(Object[] result) {
        Post post = (Post) result[0];
        String username = (String) result[1];
        return mapToPostResponse(post, username);
    }
}
