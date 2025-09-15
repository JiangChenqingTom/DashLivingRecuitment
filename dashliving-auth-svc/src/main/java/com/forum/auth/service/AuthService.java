package com.forum.auth.service;

import com.forum.auth.repository.UserRepository;
import com.forum.common.dto.request.LoginRequest;
import com.forum.common.dto.request.RegisterRequest;
import com.forum.common.dto.response.JwtResponse;
import com.forum.common.exception.BadRequestException;
import com.forum.common.exception.UserNotFoundException;
import com.forum.common.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(() -> new UserNotFoundException("User does not exist: " + username));

        String jwt = tokenCacheService.getOrGenerateToken(username, loginRequest.getPassword());
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
