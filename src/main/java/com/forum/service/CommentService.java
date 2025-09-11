package com.forum.service;

import com.forum.dto.request.CommentRequest;
import com.forum.dto.response.CommentResponse;
import com.forum.exception.BadRequestException;
import com.forum.exception.ResourceNotFoundException;
import com.forum.model.Comment;
import com.forum.model.Post;
import com.forum.model.User;
import com.forum.repository.CommentRepository;
import com.forum.repository.PostRepository;
import com.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService{

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    @CacheEvict(value = "posts", key = "#postId")
    public CommentResponse createComment(Long postId, CommentRequest commentRequest, Long userId) {
        // Verify post exists
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        // Check if parent comment exists if provided
        if (commentRequest.getParentId() != null) {
            commentRepository.findById(commentRequest.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
        }
        
        // Create new comment
        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(commentRequest.getContent());
        comment.setParentId(commentRequest.getParentId());
        
        Comment savedComment = commentRepository.save(comment);
        
        return new CommentResponse(
            savedComment.getId(),
            savedComment.getPostId(),
            savedComment.getUserId(),
            user.getUsername(),
            savedComment.getContent(),
            savedComment.getCreatedAt(),
            savedComment.getUpdatedAt(),
            savedComment.getParentId(),
            Collections.emptyList()
        );
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        // Verify post exists
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found with id: " + postId);
        }
        
        // Get all comments with usernames for the post
        List<Object[]> results = commentRepository.findCommentsWithUsernamesByPostId(postId);
        
        // Map results to CommentResponse objects
        List<CommentResponse> allComments = results.stream().map(result -> {
            Comment comment = (Comment) result[0];
            String username = (String) result[1];
            
            return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getUserId(),
                username,
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt(),
                comment.getParentId(),
                new ArrayList<>() // Temporary empty list for replies
            );
        }).collect(Collectors.toList());
        
        // Organize comments into a hierarchical structure
        return buildCommentHierarchy(allComments);
    }

    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public CommentResponse updateComment(Long commentId, CommentRequest commentRequest, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        // Check if user is the author
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new BadRequestException("You are not authorized to update this comment");
        }
        
        // Update comment content
        comment.setContent(commentRequest.getContent());
        Comment updatedComment = commentRepository.save(comment);
        
        // Get username for response
        String username = userRepository.findById(userId)
                .map(User::getUsername)
                .orElse("unknown");
        
        return new CommentResponse(
            updatedComment.getId(),
            updatedComment.getPostId(),
            updatedComment.getUserId(),
            username,
            updatedComment.getContent(),
            updatedComment.getCreatedAt(),
            updatedComment.getUpdatedAt(),
            updatedComment.getParentId(),
            Collections.emptyList()
        );
    }

    @Transactional
    @CacheEvict(value = "posts", allEntries = true)
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found with id: " + commentId));
        
        // Check if user is the author
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new BadRequestException("You are not authorized to delete this comment");
        }
        
        commentRepository.delete(comment);
    }

    // Helper method to build comment hierarchy with replies
    private List<CommentResponse> buildCommentHierarchy(List<CommentResponse> allComments) {
        // Create a map of comments by their ID
        Map<Long, CommentResponse> commentMap = new HashMap<>();
        for (CommentResponse comment : allComments) {
            commentMap.put(comment.getId(), comment);
        }
        
        // Build the hierarchy
        List<CommentResponse> rootComments = new ArrayList<>();
        for (CommentResponse comment : allComments) {
            Long parentId = comment.getParentId();
            if (parentId == null) {
                // This is a root comment
                rootComments.add(comment);
            } else {
                // This is a reply, add it to its parent's replies list
                CommentResponse parent = commentMap.get(parentId);
                if (parent != null) {
                    parent.getReplies().add(comment);
                }
            }
        }
        
        return rootComments;
    }
}
