package com.example.demo.repository;

import com.example.demo.entity.Post;
import com.example.demo.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PostRepositoryTest {

    @Autowired
    PostRepository postRepository;

    @Autowired
    AuthRepository authRepository;

    @Autowired
    CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
        authRepository.deleteAll();
    }

    private User createOwner() {
        return authRepository.save(
                new User("owner" + System.nanoTime() + "@mail.com", "hashed-password")
        );
    }

    private Post createPost(String title, String body) {
        User owner = createOwner();

        Post post = new Post(title, body);
        post.setOwner(owner);

        return postRepository.save(post);
    }

    @Test
    void save_shouldPersistPost() {
        Post saved = createPost("Test title", "Test Body");

        Assertions.assertNotNull(saved.getId());
        Assertions.assertEquals("Test title", saved.getTitle());
        Assertions.assertNotNull(saved.getOwner());
    }

    @Test
    void findAll_shouldReturnAllPosts() {
        createPost("Title 1", "Body 1");
        createPost("Title 2", "Body 2");

        List<Post> posts = postRepository.findAll();

        Assertions.assertEquals(2, posts.size());
    }

    @Test
    void findById_shouldReturnPost() {
        Post post = createPost("Title", "Body");

        Optional<Post> found = postRepository.findById(post.getId());

        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals("Title", found.get().getTitle());
    }

    @Test
    void update_shouldModifyPost() {
        Post post = createPost("Old title", "Old body");

        post.setTitle("New title");
        postRepository.save(post);

        Post updated = postRepository.findById(post.getId()).get();

        Assertions.assertEquals("New title", updated.getTitle());
    }

    @Test
    void delete_shouldRemovePost() {
        Post post = createPost("Title", "Body");

        postRepository.delete(post);

        Optional<Post> result = postRepository.findById(post.getId());

        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findById_shouldReturnEmpty_whenNotExists() {
        Optional<Post> result = postRepository.findById(999L);

        Assertions.assertTrue(result.isEmpty());
    }
}