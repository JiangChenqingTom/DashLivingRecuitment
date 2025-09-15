package com.forum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forum.config.AuthEntryPointJwt;
import com.forum.dto.request.CommentRequest;
import com.forum.dto.response.CommentResponse;
import com.forum.model.User;
import com.forum.service.CommentService;
import com.forum.repository.UserRepository;
import com.forum.service.UserDetailsServiceImpl;
import com.forum.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CommentService commentService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    UserDetailsServiceImpl userDetailsServiceImpl;

    @MockBean
    AuthEntryPointJwt authEntryPointJwt;

    @MockBean
    JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_USERNAME = "testUser";
    private final Long TEST_USER_ID = 1L;
    private final Long TEST_POST_ID = 100L;
    private final Long TEST_COMMENT_ID = 1000L;

    private CommentRequest commentRequest;
    private CommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(TEST_USERNAME, null, List.of())
        );

        commentRequest = new CommentRequest();
        commentRequest.setContent("这是一条测试评论");

        commentResponse = new CommentResponse();
        commentResponse.setId(TEST_COMMENT_ID);
        commentResponse.setContent(commentRequest.getContent());
        commentResponse.setUserId(TEST_USER_ID);
        commentResponse.setPostId(TEST_POST_ID);
    }

    @Test
    void createComment_ShouldReturnCreatedComment() throws Exception {
        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
        when(commentService.createComment(eq(TEST_POST_ID), any(CommentRequest.class), eq(TEST_USER_ID)))
                .thenReturn(commentResponse);

        mockMvc.perform(post("/api/posts/{postId}/comments", TEST_POST_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_COMMENT_ID))
                .andExpect(jsonPath("$.content").value(commentRequest.getContent()))
                .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
                .andExpect(jsonPath("$.postId").value(TEST_POST_ID));
    }

    @Test
    void getCommentsByPostId_ShouldReturnCommentList() throws Exception {
        List<CommentResponse> commentResponses = Arrays.asList(commentResponse);

        when(commentService.getCommentsByPostId(TEST_POST_ID)).thenReturn(commentResponses);

        mockMvc.perform(get("/api/posts/{postId}/comments", TEST_POST_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(TEST_COMMENT_ID));
    }

    @Test
    void updateComment_ShouldReturnUpdatedComment() throws Exception {
        String updatedContent = "这是更新后的评论内容";
        commentRequest.setContent(updatedContent);
        commentResponse.setContent(updatedContent);

        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
        when(commentService.updateComment(eq(TEST_POST_ID), eq(TEST_COMMENT_ID),
                any(CommentRequest.class), eq(TEST_USER_ID)))
                .thenReturn(commentResponse);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", TEST_POST_ID, TEST_COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TEST_COMMENT_ID))
                .andExpect(jsonPath("$.content").value(updatedContent));
    }

    @Test
    void deleteComment_ShouldReturnNoContent() throws Exception {
        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", TEST_POST_ID, TEST_COMMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}
