package com.forum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostWithUserName {
    private Post post;
    private String username;
}
