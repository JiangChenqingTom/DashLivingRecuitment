package com.forum.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class JwtResponse implements Serializable {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String username;
    private String email;
}
