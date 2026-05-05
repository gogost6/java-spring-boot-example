package com.example.demo.service;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.repository.CommentRepository;
import com.example.demo.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    CommentRepository commentRepository;
    PostRepository postRepository;

    CommentService(CommentRepository commentRepository,  PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public List<Comment> getCommentsByPostId(Long postId) {
        Post post = this.postRepository.findById(postId).orElseThrow();

        return this.commentRepository.findByPostId(post.getId());
    }

    public Comment getCommentById(Long comment_id) {
        return this.commentRepository.findById(comment_id).orElseThrow();
    }

    public Comment create(Long postId, String content) {
        Post post = this.postRepository.findById(postId).orElseThrow();
        Comment comment = new Comment(post, content);

        return commentRepository.save(comment);
    }

    public Comment update(Long commentId, String content) {
        Comment comment = this.commentRepository.findById(commentId).orElseThrow();
        comment.setContent(content);

        return commentRepository.save(comment);
    }

    public void delete(Long commentId) {
        Comment comment = this.commentRepository.findById(commentId).orElseThrow();
        commentRepository.delete(comment);
    }
}
