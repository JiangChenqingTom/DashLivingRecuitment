package com.forum.util;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private Authentication authentication;

    private UserDetails userDetails;
    private String validToken;
    private String invalidToken = "invalid.token.here";
    private String expiredToken;

    @BeforeEach
    void setUp() {
        jwtUtil.jwtSecret = "testSecretKeyWithEnoughLengthToMeetHS256Requirements";
        jwtUtil.jwtExpirationMs = 3600000; // 1小时

        userDetails = new User("testUser", "password", Collections.emptyList());
        when(authentication.getPrincipal()).thenReturn(userDetails);

        validToken = jwtUtil.generateJwtToken(authentication);

        jwtUtil.jwtExpirationMs = 1;
        expiredToken = jwtUtil.generateJwtToken(authentication);

        jwtUtil.jwtExpirationMs = 3600000;
    }

    @Test
    void generateJwtToken_ShouldReturnValidToken() {
        String token = jwtUtil.generateJwtToken(authentication);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT应该有3个部分
    }

    @Test
    void getUserNameFromJwtToken_WithValidToken_ShouldReturnCorrectUsername() {
        String username = jwtUtil.getUserNameFromJwtToken(validToken);

        assertEquals("testUser", username);
    }

    @Test
    void getUserNameFromJwtToken_WithInvalidToken_ShouldThrowException() {
        assertThrows(JwtException.class, () -> {
            jwtUtil.getUserNameFromJwtToken(invalidToken);
        });
    }

    @Test
    void validateJwtToken_WithValidToken_ShouldReturnTrue() {
        assertTrue(jwtUtil.validateJwtToken(validToken));
    }

    @Test
    void validateJwtToken_WithInvalidToken_ShouldThrowException() {
        assertThrows(JwtException.class, () -> {
            jwtUtil.validateJwtToken(invalidToken);
        });
    }

    @Test
    void validateJwtToken_WithExpiredToken_ShouldThrowException() throws InterruptedException {
        Thread.sleep(2);

        assertThrows(JwtException.class, () -> {
            jwtUtil.validateJwtToken(expiredToken);
        });
    }

    @Test
    void validateJwtToken_WithEmptyToken_ShouldThrowException() {
        assertThrows(JwtException.class, () -> {
            jwtUtil.validateJwtToken("");
        });
    }

    @Test
    void validateJwtToken_WithTamperedToken_ShouldThrowException() {
        String tamperedToken = validToken + "tamper";

        assertThrows(JwtException.class, () -> {
            jwtUtil.validateJwtToken(tamperedToken);
        });
    }

    @Test
    void validateJwtToken_WithDifferentSecret_ShouldThrowException() {
        String originalSecret = jwtUtil.jwtSecret;
        jwtUtil.jwtSecret = "differentSecretKeyThatShouldFailVerification";

        assertThrows(JwtException.class, () -> {
            jwtUtil.validateJwtToken(validToken);
        });

        jwtUtil.jwtSecret = originalSecret;
    }
}
