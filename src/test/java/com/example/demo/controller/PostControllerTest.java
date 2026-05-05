package com.example.demo.controller;

import com.example.demo.entity.Comment;
import com.example.demo.entity.Post;
import com.example.demo.service.CommentService;
import com.example.demo.service.PostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = PostController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CommentService commentService;

    @Test
    void getAllPosts_returnsPosts() {
        when(postService.getAllPosts()).thenReturn(List.of(
                new Post("Title 1", "Body 1"),
                new Post("Title 2", "Body 2")
        ));

        mvc.get()
                .uri("/api/posts")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].title").isEqualTo("Title 1");
    }

    @Test
    void createPost_returnsPost() {
        when(postService.createPost(any(Post.class)))
                .thenReturn(new Post("New title", "New body"));

        mvc.post()
                .uri("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "New title",
                          "body": "New body"
                        }
                        """)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.title").isEqualTo("New title");
    }

    @Test
    void updatePost_whenExists_returnsPost() {
        when(postService.updatePost(eq(1L), any(Post.class)))
                .thenReturn(Optional.of(new Post("Updated", "Updated body")));

        mvc.put()
                .uri("/api/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "Updated",
                          "body": "Updated body"
                        }
                        """)
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.title").isEqualTo("Updated");
    }

    @Test
    void updatePost_whenMissing_returns404() {
        when(postService.updatePost(eq(999L), any(Post.class)))
                .thenReturn(Optional.empty());

        mvc.put()
                .uri("/api/posts/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "Updated",
                          "body": "Updated body"
                        }
                        """)
                .assertThat()
                .hasStatus(404);
    }

    @Test
    void deletePost_whenExists_returns204() {
        when(postService.deletePost(1L)).thenReturn(true);

        mvc.delete()
                .uri("/api/posts/1")
                .assertThat()
                .hasStatus(204);
    }

    @Test
    void deletePost_whenMissing_returns404() {
        when(postService.deletePost(999L)).thenReturn(false);

        mvc.delete()
                .uri("/api/posts/999")
                .assertThat()
                .hasStatus(404);
    }

    @Test
    void getCommentsByPostId_returnsComments() {
        Comment comment = new Comment();
        comment.setContent("Hello");

        when(commentService.getCommentsByPostId(1L))
                .thenReturn(List.of(comment));

        mvc.get()
                .uri("/api/posts/1/comments")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].content").isEqualTo("Hello");
    }
}