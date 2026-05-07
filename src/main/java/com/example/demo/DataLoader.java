package com.example.demo;

import com.example.demo.entity.Comment;
import com.example.demo.entity.User;
import com.example.demo.repository.AuthRepository;
import com.example.demo.repository.CommentRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final AuthRepository authRepository;

    public DataLoader(PostRepository postRepository, CommentRepository commentRepository,  AuthRepository authRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.authRepository = authRepository;
    }

    @Override
    public void run(String @NonNull ... args) {
        if (postRepository.count() == 0) {
            User user = authRepository.findByEmail("example@gmail.com").orElseThrow();
            Post postOne = postRepository.save(new Post(user, "First post", "Hello from PostgreSQL"));
            Post postTwo = postRepository.save(new Post(user, "Second post", "Spring Boot is connected"));
            commentRepository.save(new Comment(postOne, "First comment"));
            commentRepository.save(new Comment(postTwo, "Second comment"));
        }
    }
}