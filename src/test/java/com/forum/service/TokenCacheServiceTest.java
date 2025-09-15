package com.forum.service;

import com.forum.util.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenCacheServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private TokenCacheService tokenCacheService;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache userTokensCache;

    @BeforeEach
    void setUp() {
        // 确保缓存管理器返回我们的模拟缓存
        when(cacheManager.getCache(eq("userTokens"))).thenReturn(userTokensCache);
    }

    @AfterEach
    void tearDown() {
        reset(authenticationManager, jwtUtil, cacheManager, userTokensCache);
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
        // 明确设置缓存获取为null
        when(userTokensCache.get(eq(username))).thenReturn(null);

        String actualToken = tokenCacheService.getOrGenerateToken(username, password);

        assertEquals(expectedToken, actualToken);
        // 验证认证和token生成逻辑被执行
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateJwtToken(auth);
    }

    @Test
    void getOrGenerateToken_WhenAuthenticationFails_ShouldThrowExceptionAndNotCache() {
        String username = "invalidUser";
        String password = "wrongPass";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));
        when(userTokensCache.get(eq(username))).thenReturn(null);

        assertThrows(BadCredentialsException.class, () ->
                tokenCacheService.getOrGenerateToken(username, password)
        );

        verify(jwtUtil, never()).generateJwtToken(any());
    }

    @Test
    void getOrGenerateToken_WhenTokenIsNull_ShouldNotCache() {
        String username = "nullTokenUser";
        String password = "pass";

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtil.generateJwtToken(auth)).thenReturn(null);
        when(userTokensCache.get(eq(username))).thenReturn(null);

        String token = tokenCacheService.getOrGenerateToken(username, password);

        assertNull(token);
    }
}
