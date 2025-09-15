package com.forum.common.dto.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class PostRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private boolean isPublished = true;
}
