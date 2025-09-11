package com.forum.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class CommentRequest {
    @NotBlank(message = "Content is required")
    private String content;
    private Long parentId;
}
