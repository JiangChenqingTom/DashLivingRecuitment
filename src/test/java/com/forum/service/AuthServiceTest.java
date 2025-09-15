package com.forum.service;

import com.forum.dto.request.LoginRequest;
import com.forum.dto.request.RegisterRequest;
import com.forum.dto.response.JwtResponse;
import com.forum.exception.BadRequestException;
import com.forum.model.User;
import com.forum.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenCacheService tokenCacheService;

    @InjectMocks
    private AuthService authService;

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_JWT = "testJwtToken";
    private static final Long TEST_USER_ID = 1L;

    @Test
    void login_ShouldReturnJwtResponse_WhenCredentialsAreValid() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        User mockUser = new User();
        mockUser.setId(TEST_USER_ID);
        mockUser.setUsername(TEST_USERNAME);
        mockUser.setEmail(TEST_EMAIL);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockUser));
        when(tokenCacheService.getOrGenerateToken(TEST_USERNAME, TEST_PASSWORD)).thenReturn(TEST_JWT);

        JwtResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals(TEST_JWT, response.getToken());
        assertEquals("Bearer", response.getType());
        assertEquals(TEST_USER_ID, response.getId());
        assertEquals(TEST_USERNAME, response.getUsername());
        assertEquals(TEST_EMAIL, response.getEmail());

    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(TEST_USERNAME);
        loginRequest.setPassword(TEST_PASSWORD);

        when(userRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> authService.login(loginRequest));
        verify(tokenCacheService, never()).getOrGenerateToken(anyString(), anyString());
    }

    @Test
    void register_ShouldReturnJwtResponse_WhenRegistrationIsSuccessful() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);
        registerRequest.setFullName("Test User");

        User savedUser = new User();
        savedUser.setId(TEST_USER_ID);
        savedUser.setUsername(TEST_USERNAME);
        savedUser.setEmail(TEST_EMAIL);
        savedUser.setFullName("Test User");
        savedUser.setActive(true);

        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(false);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenCacheService.getOrGenerateToken(TEST_USERNAME, TEST_PASSWORD)).thenReturn(TEST_JWT);

        JwtResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals(TEST_JWT, response.getToken());
        assertEquals(TEST_USER_ID, response.getId());
        assertEquals(TEST_USERNAME, response.getUsername());

        verify(userRepository).existsByUsername(TEST_USERNAME);
        verify(userRepository).existsByEmail(TEST_EMAIL);
        verify(passwordEncoder).encode(TEST_PASSWORD);
        verify(userRepository).save(any(User.class));
        verify(tokenCacheService).getOrGenerateToken(TEST_USERNAME, TEST_PASSWORD);
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);

        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.register(registerRequest));
        assertEquals("Username is already exist!", exception.getMessage());

        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(TEST_USERNAME);
        registerRequest.setEmail(TEST_EMAIL);
        registerRequest.setPassword(TEST_PASSWORD);

        when(userRepository.existsByUsername(TEST_USERNAME)).thenReturn(false);
        when(userRepository.existsByEmail(TEST_EMAIL)).thenReturn(true);

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> authService.register(registerRequest));
        assertEquals("Email is already in use!", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
    }
}