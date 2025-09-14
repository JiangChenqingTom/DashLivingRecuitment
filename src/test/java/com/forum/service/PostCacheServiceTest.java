package com.forum.service;

import com.forum.dto.response.PostResponse;
import com.forum.exception.ResourceNotFoundException;
import com.forum.model.Post;
import com.forum.model.PostWithUserName;
import com.forum.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostCacheServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostCacheService postCacheService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache postsCache;  // 保留实际使用的缓存模拟

    @BeforeEach
    void setUp() {
        // 只保留PostCacheService实际使用的缓存模拟
        lenient().when(cacheManager.getCache(eq("posts"))).thenReturn(postsCache);
    }

    @AfterEach
    void tearDown() {
        // 清理缓存时增加空指针检查
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
        // 重置所有模拟对象
        reset(postRepository, cacheManager, postsCache);
    }

    @Test
    void getPostByIdFromCacheOrDB_WhenPostExists_ShouldReturnPostResponse() {
        Long postId = 1L;
        String username = "testUser";
        Post post = createSamplePost(postId, 20);
        PostWithUserName postWithUserName = new PostWithUserName(post, username);

        when(postRepository.findPostWithUsernameById(postId)).thenReturn(Optional.of(postWithUserName));

        PostResponse result = postCacheService.getPostByIdFromCacheOrDB(postId);

        assertNotNull(result);
        assertEquals(postId, result.getId());
        assertEquals(username, result.getAuthorUsername());
        verify(postRepository, times(1)).findPostWithUsernameById(postId);
    }

    @Test
    void getPostByIdFromCacheOrDB_WhenPostNotFound_ShouldThrowException() {
        Long postId = 999L;
        when(postRepository.findPostWithUsernameById(postId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                postCacheService.getPostByIdFromCacheOrDB(postId)
        );
        verify(postRepository, times(1)).findPostWithUsernameById(postId);
    }

    @Test
    void getPostByIdFromCacheOrDB_WhenViewCountLessThan10_ShouldNotBeCached() {
        Long postId = 2L;
        Post post = createSamplePost(postId, 5);
        PostWithUserName postWithUserName = new PostWithUserName(post, "testUser");

        when(postRepository.findPostWithUsernameById(postId)).thenReturn(Optional.of(postWithUserName));

        PostResponse firstResult = postCacheService.getPostByIdFromCacheOrDB(postId);
        PostResponse secondResult = postCacheService.getPostByIdFromCacheOrDB(postId);

        assertNotNull(firstResult);
        assertNotNull(secondResult);
        verify(postRepository, times(2)).findPostWithUsernameById(postId);
    }

    private Post createSamplePost(Long id, int viewCount) {
        Post post = new Post();
        post.setId(id);
        post.setTitle("Test Post " + id);
        post.setContent("Test Content");
        post.setAuthorId(1L);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        post.setViewCount(viewCount);
        post.setPublished(true);
        return post;
    }
}
