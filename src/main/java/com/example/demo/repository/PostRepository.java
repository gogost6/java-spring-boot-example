package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Post;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
        SELECT p
        FROM Post p
        LEFT JOIN FETCH p.comments
    """)
    List<Post> findAllWithComments();
}