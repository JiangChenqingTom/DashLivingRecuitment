package com.forum.controller;

import com.forum.dto.request.PostRequest;
import com.forum.dto.response.PostResponse;
import com.forum.repository.UserRepository;
import com.forum.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest postRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByUsername(username).orElseThrow().getId();
        return ResponseEntity.ok(postService.createPost(postRequest, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPublishedPosts(Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPublishedPosts(pageable));
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<Page<PostResponse>> getPostsByAuthor(
            @PathVariable Long authorId, 
            Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsByAuthor(authorId, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id, 
            @Valid @RequestBody PostRequest postRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByUsername(username).orElseThrow().getId();
        return ResponseEntity.ok(postService.updatePost(id, postRequest, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userRepository.findByUsername(username).orElseThrow().getId();
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }
}
