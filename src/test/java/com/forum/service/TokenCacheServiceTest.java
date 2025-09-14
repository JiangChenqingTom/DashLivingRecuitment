package com.forum.service;

import com.forum.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class TokenCacheServiceTest {

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private TokenCacheService tokenCacheService;

    @Autowired
    private CacheManager cacheManager;

    @AfterEach
    void tearDown() {
        cacheManager.getCache("userTokens").clear();
    }

    @Test
    void getOrGenerateToken_WhenFirstCall_ShouldAuthenticateAndGenerateToken() {
        String username = "testUser";
        String password = "testPass";
        String expectedToken = "jwt_token_123";

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtUtil.generateJwtToken(auth)).thenReturn(expectedToken);

        String actualToken = tokenCacheService.getOrGenerateToken(username, password);

        assertEquals(expectedToken, actualToken);
        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, times(1)).generateJwtToken(auth);

        assertNotNull(cacheManager.getCache("userTokens").get(username));
        assertEquals(expectedToken, cacheManager.getCache("userTokens").get(username).get());
    }

    @Test
    void getOrGenerateToken_WhenCalledTwice_ShouldUseCachedToken() {
        String username = "cachedUser";
        String password = "cachedPass";
        String expectedToken = "cached_jwt_token";

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any()))
                .thenReturn(auth);
        when(jwtUtil.generateJwtToken(auth)).thenReturn(expectedToken);

        String firstToken = tokenCacheService.getOrGenerateToken(username, password);
        String secondToken = tokenCacheService.getOrGenerateToken(username, password);

        assertEquals(expectedToken, firstToken);
        assertEquals(firstToken, secondToken);

        verify(authenticationManager, times(1)).authenticate(any());
        verify(jwtUtil, times(1)).generateJwtToken(auth);
    }

    @Test
    void getOrGenerateToken_WhenAuthenticationFails_ShouldThrowExceptionAndNotCache() {
        String username = "invalidUser";
        String password = "wrongPass";

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () ->
                tokenCacheService.getOrGenerateToken(username, password)
        );

        assertNull(cacheManager.getCache("userTokens").get(username));
        verify(jwtUtil, never()).generateJwtToken(any());
    }

    @Test
    void getOrGenerateToken_WhenTokenIsNull_ShouldNotCache() {
        String username = "nullTokenUser";
        String password = "pass";

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtil.generateJwtToken(auth)).thenReturn(null);

        String token = tokenCacheService.getOrGenerateToken(username, password);

        assertNull(token);
        assertNull(cacheManager.getCache("userTokens").get(username));
    }
}
