package com.forum.service;

import com.forum.dto.request.LoginRequest;
import com.forum.dto.request.RegisterRequest;
import com.forum.dto.response.JwtResponse;
import com.forum.exception.BadRequestException;
import com.forum.model.User;
import com.forum.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    TokenCacheService tokenCacheService;

    public JwtResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();

        String jwt = tokenCacheService.getOrGenerateToken(username, loginRequest.getPassword());

        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return new JwtResponse(jwt, "Bearer", user.getId(), user.getUsername(), user.getEmail());
    }

    @Transactional
    public JwtResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already exist!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        String jwt = tokenCacheService.getOrGenerateToken(registerRequest.getUsername(), registerRequest.getPassword());

        return new JwtResponse(jwt, "Bearer", savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }

}
