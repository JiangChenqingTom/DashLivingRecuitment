package com.forum.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableCaching
@EntityScan("com.forum.common.model")
@ComponentScan(basePackages = {
        "com.forum.gateway",
        "com.forum.common.util"
})
public class ForumGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ForumGatewayApplication.class, args);
    }
}
