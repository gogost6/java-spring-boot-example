package com.example.demo.repository;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.Optional;
import java.util.Set;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CommentRepositoryTest {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private AuthRepository authRepository;

    @BeforeEach
    public void setup() {
        commentRepository.deleteAll();
    }

    private User createOwner() {
        return authRepository.save(
                new User("owner" + System.nanoTime() + "@mail.com", "hashed-password", Set.of(Role.USER))
        );
    }

    private Post createPost(String title, String body) {
        User owner = createOwner();

        Post post = new Post(title, body);
        post.setOwner(owner);

        return postRepository.save(post);
    }

    private Comment createComment(String body) {
        User owner = createOwner();
        Post post = createPost("Title", body);
        Comment comment = new Comment(post, owner, body);

        return commentRepository.save(comment);
    }

    @Test
    void save_shouldPersistComment() {
        String content = "This is a comment";
        Comment comment = createComment(content);

        Assertions.assertNotNull(comment.getId());
        Assertions.assertNotNull(comment.getOwner());
        Assertions.assertNotNull(comment.getPost());
        Assertions.assertEquals(content, comment.getContent());
    }

    @Test
    void findById_shouldReturnComment() {
        String content = "This is a comment";
        Comment comment = createComment(content);
        Assertions.assertNotNull(comment.getId());
        Assertions.assertNotNull(comment.getOwner());
        Assertions.assertEquals(content, comment.getContent());
    }

    @Test
    void findAll_shouldReturnAllComments() {
        createComment("This is a comment");
        createComment("This is a another comment");
        createComment("This is another again comment");

        Assertions.assertEquals(3, commentRepository.findAll().size());
    }

    @Test
    void update_shouldUpdateComment() {
        String content = "This is a comment";
        String updatedContent = "This is another comment";
        Comment comment = createComment(content);
        comment.setContent(updatedContent);

        Assertions.assertEquals(updatedContent, comment.getContent());
    }

    @Test
    void deleteById_shouldDeleteComment() {
        String content = "This is a comment";
        Comment comment = createComment(content);
        commentRepository.delete(comment);
        Optional<Comment> result = commentRepository.findById(comment.getId());
        Assertions.assertFalse(result.isPresent());
    }
}
