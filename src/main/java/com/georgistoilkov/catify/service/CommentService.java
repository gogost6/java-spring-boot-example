package com.georgistoilkov.catify.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.georgistoilkov.catify.dto.CommentResponse;
import com.georgistoilkov.catify.entity.Comment;
import com.georgistoilkov.catify.entity.Post;
import com.georgistoilkov.catify.entity.User;
import com.georgistoilkov.catify.exception.ForbiddenException;
import com.georgistoilkov.catify.exception.ResourceNotFoundException;
import com.georgistoilkov.catify.repository.AuthRepository;
import com.georgistoilkov.catify.repository.CommentRepository;
import com.georgistoilkov.catify.repository.PostRepository;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AuthRepository authRepository;

    CommentService(CommentRepository commentRepository, PostRepository postRepository, AuthRepository authRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.authRepository = authRepository;
    }

    private CommentResponse toResponse(Comment c) {
        return new CommentResponse(c.getId(), c.getContent(), c.getOwner().getEmail(), c.getCreatedAt(),
                c.getUpdatedAt());
    }

    public List<CommentResponse> getCommentsByPostId(Long postId) {
        Post post = this.postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        return this.commentRepository.findByPostId(post.getId())
                .stream().map(this::toResponse).toList();
    }

    public Page<CommentResponse> getCommentsByPostId(Long postId, Pageable pageable) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post not found");
        }
        return commentRepository.findByPostId(postId, pageable).map(this::toResponse);
    }

    public CommentResponse getCommentById(Long commentId) {
        Comment comment = this.commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));
        return toResponse(comment);
    }

    public CommentResponse create(Long postId, String email, String content) {
        Post post = this.postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
        User user = this.authRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Comment comment = new Comment(post, user, content);
        return toResponse(commentRepository.save(comment));
    }

    public CommentResponse update(Long commentId, String email, String content) {
        Comment comment = this.commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getOwner().getEmail().equals(email)) {
            throw new ForbiddenException("You are not the owner of this comment!");
        }

        comment.setContent(content);
        return toResponse(commentRepository.save(comment));
    }

    public void delete(Long commentId, String email) {
        Comment comment = this.commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getOwner().getEmail().equals(email)) {
            throw new ForbiddenException("You are not the owner of this comment!");
        }

        commentRepository.delete(comment);
    }
}
