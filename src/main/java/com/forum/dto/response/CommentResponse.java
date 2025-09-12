package com.forum.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class CommentResponse implements Serializable {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long parentId;
    private List<CommentResponse> replies;
}
