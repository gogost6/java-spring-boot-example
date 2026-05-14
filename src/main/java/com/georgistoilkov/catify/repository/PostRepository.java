package com.georgistoilkov.catify.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.georgistoilkov.catify.entity.Post;
import com.georgistoilkov.catify.entity.User;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
        SELECT p
        FROM Post p
        LEFT JOIN FETCH p.comments
    """)
    List<Post> findAllWithComments();

    @Query("""
        SELECT p FROM Post p
        WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%'))
           OR LOWER(p.body)  LIKE LOWER(CONCAT('%', :search, '%'))
    """)
    Page<Post> findBySearch(@Param("search") String search, Pageable pageable);

    void deleteByOwner(User owner);
}