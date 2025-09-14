package com.forum.service;

import com.forum.dto.response.PostResponse;
import com.forum.exception.ResourceNotFoundException;
import com.forum.model.Post;
import com.forum.model.PostWithUserName;
import com.forum.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@EnableCaching
class PostCacheServiceTest {

    @MockBean
    private PostRepository postRepository;

    @Autowired
    private PostCacheService postCacheService;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void tearDown() {
        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
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

    @Test
    void getPostByIdFromCacheOrDB_WhenCalledTwiceWithSameId_ShouldCallRepositoryOnce() {
        Long postId = 3L;
        Post post = createSamplePost(postId, 15); // 满足缓存条件
        PostWithUserName postWithUserName = new PostWithUserName(post, "testUser");

        when(postRepository.findPostWithUsernameById(postId)).thenReturn(Optional.of(postWithUserName));

        PostResponse firstResult = postCacheService.getPostByIdFromCacheOrDB(postId);
        PostResponse secondResult = postCacheService.getPostByIdFromCacheOrDB(postId);

        assertEquals(firstResult, secondResult);
        verify(postRepository, times(1)).findPostWithUsernameById(postId); // 现在会正确调用一次
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