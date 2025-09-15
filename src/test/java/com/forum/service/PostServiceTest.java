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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostCacheService postCacheService;

    @InjectMocks
    private PostService postService;

    private User testUser;
    private Post testPost;
    private PostRequest testPostRequest;
    private PostWithUserName testPostWithUserName;

    @BeforeEach
    void setUp() {
        postRepository.deleteAll();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Title");
        testPost.setContent("Test Content");
        testPost.setAuthorId(1L);
        testPost.setPublished(true);
        testPost.setViewCount(0);
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());

        testPostRequest = new PostRequest();
        testPostRequest.setTitle("New Title");
        testPostRequest.setContent("New Content");

        testPostWithUserName = new PostWithUserName();
        testPostWithUserName.setPost(testPost);
        testPostWithUserName.setUsername("testUser");
    }

    @Test
    void createPost_ShouldReturnPostResponse_WhenUserExists() {
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostResponse result = postService.createPost(testPostRequest, 1L);

        assertNotNull(result);
        assertEquals(testPost.getTitle(), result.getTitle());
        assertEquals(testPost.getContent(), result.getContent());
        assertEquals(testUser.getUsername(), result.getAuthorUsername());
        verify(userRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void createPost_ShouldThrowResourceNotFoundException_WhenUserDoesNotExist() {
        
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.createPost(testPostRequest, 1L);
        });
        verify(userRepository, times(1)).findById(1L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void getPostById_ShouldReturnPostResponse_WhenPostExists() {
        
        PostResponse expectedResponse = new PostResponse();
        expectedResponse.setId(1L);
        expectedResponse.setTitle("Test Title");

        when(postCacheService.getPostByIdFromCacheOrDB(1L)).thenReturn(expectedResponse);
        when(postRepository.incrementViewCount(1L)).thenReturn(2);

        PostResponse result = postService.getPostById(1L);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getTitle(), result.getTitle());
        verify(postRepository, times(1)).incrementViewCount(1L);
        verify(postCacheService, times(1)).getPostByIdFromCacheOrDB(1L);
    }

    @Test
    void incrementViewCount_ShouldUpdateCount_WhenPostExists() {
        when(postRepository.incrementViewCount(1L)).thenReturn(1);

        assertDoesNotThrow(() -> postService.incrementViewCount(1L));

        verify(postRepository, times(1)).incrementViewCount(1L);
    }

    @Test
    void incrementViewCount_ShouldThrowException_WhenPostDoesNotExist() {
        when(postRepository.incrementViewCount(1L)).thenReturn(0);

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.incrementViewCount(1L);
        });
        verify(postRepository, times(1)).incrementViewCount(1L);
    }

    @Test
    void getAllPublishedPosts_ShouldReturnPageOfPostResponses() {
        
        Pageable pageable = Pageable.ofSize(10);
        List<PostWithUserName> postList = Arrays.asList(testPostWithUserName);
        Page<PostWithUserName> postPage = new PageImpl<>(postList, pageable, postList.size());

        when(postRepository.findAllPublishedPostsWithAuthors(pageable)).thenReturn(postPage);

        Page<PostResponse> result = postService.getAllPublishedPosts(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(testPost.getTitle(), result.getContent().get(0).getTitle());
        verify(postRepository, times(1)).findAllPublishedPostsWithAuthors(pageable);
    }

    @Test
    void updatePost_ShouldReturnUpdatedPost_WhenUserIsAuthor() {
        
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(testPost);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        PostResponse result = postService.updatePost(1L, testPostRequest, 1L);

        assertNotNull(result);
        assertEquals(testPostRequest.getTitle(), result.getTitle());
        assertEquals(testPostRequest.getContent(), result.getContent());
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).save(any(Post.class));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void updatePost_ShouldThrowException_WhenPostDoesNotExist() {
        
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.updatePost(1L, testPostRequest, 1L);
        });
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePost_ShouldThrowException_WhenUserIsNotAuthor() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        assertThrows(SecurityException.class, () -> {
            postService.updatePost(1L, testPostRequest, 2L); // 不同的用户ID
        });
        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost_ShouldDeletePost_WhenUserIsAuthor() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        assertDoesNotThrow(() -> postService.deletePost(1L, 1L));

        verify(postRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).deleteByPostId(1L);
        verify(postRepository, times(1)).delete(testPost);
    }

    @Test
    void deletePost_ShouldThrowException_WhenPostDoesNotExist() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            postService.deletePost(1L, 1L);
        });
        verify(postRepository, times(1)).findById(1L);
        verify(commentRepository, never()).deleteByPostId(anyLong());
        verify(postRepository, never()).delete(any(Post.class));
    }

    @Test
    void deletePost_ShouldThrowException_WhenUserIsNotAuthor() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        assertThrows(SecurityException.class, () -> {
            postService.deletePost(1L, 2L); // 不同的用户ID
        });
        verify(postRepository, times(1)).findById(1L);
        verify(commentRepository, never()).deleteByPostId(anyLong());
        verify(postRepository, never()).delete(any(Post.class));
    }
}
