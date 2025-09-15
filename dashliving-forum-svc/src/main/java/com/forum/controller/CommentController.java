package com.forum.controller;

import com.forum.common.dto.request.CommentRequest;
import com.forum.common.dto.response.CommentResponse;
import com.forum.repository.UserRepository;
import com.forum.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest commentRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByUsername(username).orElseThrow().getId();
        return ResponseEntity.ok(commentService.createComment(postId, commentRequest, userId));
    }

    @GetMapping
    public ResponseEntity<List<CommentResponse>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest commentRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByUsername(username).orElseThrow().getId();
        return ResponseEntity.ok(commentService.updateComment(postId, commentId, commentRequest, userId));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByUsername(username).orElseThrow().getId();
        commentService.deleteComment(postId, commentId, userId);
        return ResponseEntity.noContent().build();
    }
}
