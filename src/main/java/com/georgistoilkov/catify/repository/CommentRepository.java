package com.georgistoilkov.catify.repository;

import com.georgistoilkov.catify.entity.Comment;
import com.georgistoilkov.catify.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
    Page<Comment> findByPostId(Long postId, Pageable pageable);

    void deleteByOwner(User owner);
}
