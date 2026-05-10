package com.example.demo;

import com.example.demo.dto.AuthRequest;
import com.example.demo.entity.Comment;
import com.example.demo.entity.User;
import com.example.demo.repository.CommentRepository;
import com.example.demo.service.AuthService;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AuthService authService;

    public DataLoader(PostRepository postRepository, CommentRepository commentRepository, AuthService authService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.authService = authService;
    }

    @Override
    public void run(String @NonNull ... args) {
        if (postRepository.count() == 0) {
            AuthRequest authRequest = new AuthRequest("example@gmail.com", "Password$123456");
            User user = authService.register(authRequest);
            Post postOne = postRepository.save(new Post(user, "First post", "Hello from PostgreSQL"));
            Post postTwo = postRepository.save(new Post(user, "Second post", "Spring Boot is connected"));
            commentRepository.save(new Comment(postOne, user, "First comment"));
            commentRepository.save(new Comment(postTwo, user, "Second comment"));
        }
    }
}