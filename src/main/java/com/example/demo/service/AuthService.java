package com.example.demo.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.AuthRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.AuthRepository;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.repository.RefreshTokenRepository;

@Service
public class AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(AuthRepository authRepository, PasswordEncoder passwordEncoder,
            PostRepository postRepository, CommentRepository commentRepository,
            RefreshTokenRepository refreshTokenRepository) {
        this.authRepository = authRepository;
        this.passwordEncoder = passwordEncoder;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public User register(AuthRequest authRequest) {
        String email = authRequest.email();
        String password = authRequest.password();
        Optional<User> user = authRepository.findByEmail(email);

        if (user.isPresent()) {
            throw new IllegalStateException("User already exists!");
        }

        String hashedPassword = passwordEncoder.encode(password);

        Set<Role> roles = authRepository.count() == 0
                ? Set.of(Role.ADMIN, Role.USER)
                : Set.of(Role.USER);
        User userEntity = new User(email, hashedPassword, roles);

        return authRepository.save(userEntity);
    }

    public User login(AuthRequest authRequest) {
        String email = authRequest.email();
        String password = authRequest.password();
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Invalid email or password!"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalStateException("Invalid email or password!");
        }

        return user;
    }

    public User updateEmail(String email, String newEmail) {
        if (email.equals(newEmail)) {
            throw new IllegalStateException("Old email same as new email!");
        }

        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Invalid email!"));
        user.setEmail(newEmail);

        return authRepository.save(user);
    }

    public User updatePassword(String email, String oldPassword, String newPassword) {
        if (oldPassword.equals(newPassword)) {
            throw new IllegalStateException("New password must be different from old password!");
        }

        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Invalid email!"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalStateException("Old password doesn't match!");
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashedPassword);

        return authRepository.save(user);
    }

    public User addRole(String email, Role role) {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        user.getRoles().add(role);

        return authRepository.save(user);
    }

    public User findByEmail(String email) {
        return authRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    @Transactional
    public void deleteUser(String email) {
        User user = authRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        commentRepository.deleteByOwner(user);
        postRepository.deleteByOwner(user);
        refreshTokenRepository.deleteByUser(user);
        authRepository.delete(user);
    }
}
