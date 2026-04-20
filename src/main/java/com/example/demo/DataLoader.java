package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Post;
import com.example.demo.repository.PostRepository;

@Component
public class DataLoader implements CommandLineRunner {

    private final PostRepository postRepository;

    public DataLoader(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) {
        if (postRepository.count() == 0) {
            postRepository.save(new Post("First post", "Hello from PostgreSQL"));
            postRepository.save(new Post("Second post", "Spring Boot is connected"));
        }
    }
}