package com.forum.service;

import com.forum.dto.request.CommentRequest;
import com.forum.dto.response.CommentResponse;
import com.forum.exception.BadRequestException;
import com.forum.exception.ResourceNotFoundException;
import com.forum.model.Comment;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private Long postId;
    private Long userId;
    private Long commentId;
    private User user;
    private Comment comment;
    private CommentRequest commentRequest;

    @BeforeEach
    void setUp() {
        postId = 1L;
        userId = 1L;
        commentId = 1L;

        user = new User();
        user.setId(userId);
        user.setUsername("testUser");

        comment = new Comment();
        comment.setId(commentId);
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent("Test comment");
        comment.setParentId(null);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        commentRequest = new CommentRequest();
        commentRequest.setContent("New comment");
        commentRequest.setParentId(null);
    }

    @Test
    void createComment_Success() {
        // Arrange
        when(postRepository.existsById(postId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // Act
        CommentResponse result = commentService.createComment(postId, commentRequest, userId);

        // Assert
        assertNotNull(result);
        assertEquals(commentId, result.getId());
        assertEquals("testUser", result.getUsername());
        assertEquals("Test comment", result.getContent());

        verify(postRepository).existsById(postId);
        verify(userRepository).findById(userId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void createComment_PostNotFound() {
        // Arrange
        when(postRepository.existsById(postId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(postId, commentRequest, userId);
        });

        verify(postRepository).existsById(postId);
        verifyNoMoreInteractions(userRepository, commentRepository);
    }

    @Test
    void createComment_UserNotFound() {
        // Arrange
        when(postRepository.existsById(postId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(postId, commentRequest, userId);
        });

        verify(postRepository).existsById(postId);
        verify(userRepository).findById(userId);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void createComment_ParentCommentNotFound() {
        // Arrange
        Long parentId = 99L;
        commentRequest.setParentId(parentId);

        when(postRepository.existsById(postId)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(commentRepository.findById(parentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.createComment(postId, commentRequest, userId);
        });

        verify(commentRepository).findById(parentId);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void getCommentsByPostId_Success() {
        // Arrange
        when(postRepository.existsById(postId)).thenReturn(true);

        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{comment, user.getUsername()});

        when(commentRepository.findCommentsWithUsernamesByPostId(postId)).thenReturn(results);

        // Act
        List<CommentResponse> result = commentService.getCommentsByPostId(postId);

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(commentId, result.get(0).getId());

        verify(postRepository).existsById(postId);
        verify(commentRepository).findCommentsWithUsernamesByPostId(postId);
    }

    @Test
    void getCommentsByPostId_PostNotFound() {
        // Arrange
        when(postRepository.existsById(postId)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.getCommentsByPostId(postId);
        });

        verify(postRepository).existsById(postId);
        verifyNoInteractions(commentRepository);
    }

    @Test
    void getCommentsByPostId_WithHierarchy() {
        // Arrange
        when(postRepository.existsById(postId)).thenReturn(true);

        // Create parent comment
        Comment parentComment = new Comment();
        parentComment.setId(1L);
        parentComment.setPostId(postId);
        parentComment.setUserId(userId);
        parentComment.setContent("Parent comment");
        parentComment.setParentId(null);
        parentComment.setCreatedAt(LocalDateTime.now());

        // Create reply comment
        Comment replyComment = new Comment();
        replyComment.setId(2L);
        replyComment.setPostId(postId);
        replyComment.setUserId(userId);
        replyComment.setContent("Reply comment");
        replyComment.setParentId(1L);
        replyComment.setCreatedAt(LocalDateTime.now());

        List<Object[]> results = new ArrayList<>();
        results.add(new Object[]{parentComment, user.getUsername()});
        results.add(new Object[]{replyComment, user.getUsername()});

        when(commentRepository.findCommentsWithUsernamesByPostId(postId)).thenReturn(results);

        // Act
        List<CommentResponse> result = commentService.getCommentsByPostId(postId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size()); // Only parent comment in root list
        assertEquals(1, result.get(0).getReplies().size()); // One reply to parent
        assertEquals(2L, result.get(0).getReplies().get(0).getId());
    }

    @Test
    void updateComment_Success() {
        // Arrange
        String updatedContent = "Updated content";
        commentRequest.setContent(updatedContent);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        CommentResponse result = commentService.updateComment(postId, commentId, commentRequest, userId);

        // Assert
        assertNotNull(result);
        assertEquals(updatedContent, result.getContent());
        assertEquals(commentId, result.getId());

        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(comment);
        verify(userRepository).findById(userId);
    }

    @Test
    void updateComment_CommentNotFound() {
        // Arrange
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.updateComment(postId, commentId, commentRequest, userId);
        });

        verify(commentRepository).findById(commentId);
        verifyNoMoreInteractions(commentRepository, userRepository);
    }

    @Test
    void updateComment_CommentNotInPost() {
        // Arrange
        Long wrongPostId = 99L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            commentService.updateComment(wrongPostId, commentId, commentRequest, userId);
        });

        verify(commentRepository).findById(commentId);
        verifyNoMoreInteractions(commentRepository, userRepository);
    }

    @Test
    void updateComment_NotAuthorized() {
        // Arrange
        Long wrongUserId = 99L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            commentService.updateComment(postId, commentId, commentRequest, wrongUserId);
        });

        verify(commentRepository).findById(commentId);
        verifyNoMoreInteractions(commentRepository, userRepository);
    }

    @Test
    void deleteComment_Success() {
        // Arrange
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act
        commentService.deleteComment(postId, commentId, userId);

        // Assert
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_CommentNotFound() {
        // Arrange
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            commentService.deleteComment(postId, commentId, userId);
        });

        verify(commentRepository).findById(commentId);
        verifyNoMoreInteractions(commentRepository);
    }

    @Test
    void deleteComment_NotAuthorized() {
        // Arrange
        Long wrongUserId = 99L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            commentService.deleteComment(postId, commentId, wrongUserId);
        });

        verify(commentRepository).findById(commentId);
        verifyNoMoreInteractions(commentRepository);
    }
}
