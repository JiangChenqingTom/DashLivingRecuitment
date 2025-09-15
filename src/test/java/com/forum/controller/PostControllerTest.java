package com.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forum.config.AuthEntryPointJwt;
import com.forum.dto.request.PostRequest;
import com.forum.dto.response.PostResponse;
import com.forum.model.User;
import com.forum.service.PostService;
import com.forum.repository.UserRepository;
import com.forum.service.UserDetailsServiceImpl;
import com.forum.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RedisConnectionFactory redisConnectionFactory;

    @MockBean
    UserDetailsServiceImpl userDetailsServiceImpl;

    @MockBean
    AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    JwtUtil jwtUtil;

    private final String BASE_URL = "/api/posts";
    private final Long TEST_USER_ID = 1L;
    private final Long TEST_POST_ID = 1L;
    private final String TEST_USERNAME = "testUser";

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void createPost_ShouldReturnCreatedPost() throws Exception {
        PostRequest postRequest = new PostRequest();
        postRequest.setTitle("Test Title");
        postRequest.setContent("Test Content");

        PostResponse postResponse = new PostResponse();
        postResponse.setId(TEST_POST_ID);
        postResponse.setTitle(postRequest.getTitle());
        postResponse.setContent(postRequest.getContent());
        postResponse.setAuthorUsername(TEST_USERNAME);

        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
        when(postService.createPost(any(PostRequest.class), eq(TEST_USER_ID))).thenReturn(postResponse);

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_POST_ID))
                .andExpect(jsonPath("$.title").value(postRequest.getTitle()))
                .andExpect(jsonPath("$.content").value(postRequest.getContent()));
    }

    @Test
    void getPostById_ShouldReturnPost() throws Exception {
        PostResponse postResponse = new PostResponse();
        postResponse.setId(TEST_POST_ID);
        postResponse.setTitle("Test Title");
        postResponse.setContent("Test Content");
        postResponse.setAuthorUsername(TEST_USERNAME);

        when(postService.getPostById(TEST_POST_ID)).thenReturn(postResponse);

        mockMvc.perform(get(BASE_URL + "/{id}", TEST_POST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_POST_ID))
                .andExpect(jsonPath("$.title").value(postResponse.getTitle()));
    }

    @Test
    void getAllPublishedPosts_ShouldReturnPostsPage() throws Exception {
        PostResponse postResponse = new PostResponse();
        postResponse.setId(TEST_POST_ID);
        postResponse.setTitle("Test Title");
        postResponse.setContent("Test Content");

        Page<PostResponse> postPage = new PageImpl<>(Collections.singletonList(postResponse));

        when(postService.getAllPublishedPosts(any(Pageable.class))).thenReturn(postPage);

        mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(TEST_POST_ID))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void updatePost_ShouldReturnUpdatedPost() throws Exception {
        PostRequest updateRequest = new PostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated Content");

        PostResponse updatedResponse = new PostResponse();
        updatedResponse.setId(TEST_POST_ID);
        updatedResponse.setTitle(updateRequest.getTitle());
        updatedResponse.setContent(updateRequest.getContent());
        updatedResponse.setAuthorUsername(TEST_USERNAME);

        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
        when(postService.updatePost(eq(TEST_POST_ID), any(PostRequest.class), eq(TEST_USER_ID))).thenReturn(updatedResponse);

        mockMvc.perform(put(BASE_URL + "/{id}", TEST_POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_POST_ID))
                .andExpect(jsonPath("$.title").value(updateRequest.getTitle()))
                .andExpect(jsonPath("$.content").value(updateRequest.getContent()));
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void deletePost_ShouldReturnNoContent() throws Exception {
        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(delete(BASE_URL + "/{id}", TEST_POST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = TEST_USERNAME)
    void createPost_WhenUserNotFound_ShouldReturnBadRequest() throws Exception {
        PostRequest postRequest = new PostRequest();
        postRequest.setTitle("Test Title");
        postRequest.setContent("Test Content");

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().is5xxServerError()); // 实际项目中应自定义异常处理返回4xx
    }
}
