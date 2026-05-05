package com.example.demo;

import com.example.demo.entity.Comment;
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

    public DataLoader(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public void run(String @NonNull ... args) {
        if (postRepository.count() == 0) {
            Post postOne = postRepository.save(new Post("First post", "Hello from PostgreSQL"));
            Post postTwo = postRepository.save(new Post("Second post", "Spring Boot is connected"));
            commentRepository.save(new Comment(postOne, "First comment"));
            commentRepository.save(new Comment(postTwo, "Second comment"));
        }
    }
}