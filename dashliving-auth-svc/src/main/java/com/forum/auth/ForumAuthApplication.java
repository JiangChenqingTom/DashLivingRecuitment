package com.forum.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCaching
@EntityScan("com.forum.common.model")
@ComponentScan(basePackages = {
        "com.forum.auth",
        "com.forum.common.util"
})
public class ForumAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForumAuthApplication.class, args);
    }
}

