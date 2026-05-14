package com.georgistoilkov.catify.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.List;
import java.util.Set;

import com.georgistoilkov.catify.dto.CommentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import com.georgistoilkov.catify.config.SecurityConfig;
import com.georgistoilkov.catify.entity.Comment;
import com.georgistoilkov.catify.entity.Post;
import com.georgistoilkov.catify.entity.Role;
import com.georgistoilkov.catify.entity.User;
import com.georgistoilkov.catify.service.CommentService;
import com.georgistoilkov.catify.service.PostService;

@WebMvcTest(controllers = PostController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class PostControllerTest {

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CommentService commentService;

    @Test
    void getAllPosts_returnsPosts() {
        User owner = new User("owner@mail.com", "pwd", Set.of(Role.USER));
        when(postService.getAllPosts(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(
                new Post(owner, "Title 1", "Body 1"),
                new Post(owner, "Title 2", "Body 2"))
        ));

        mvc.get().uri("/api/posts")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[0].title").isEqualTo("Title 1");
    }

    @Test
    void createPost_returnsPost() {
        User owner = new User("owner@mail.com", "pwd", Set.of(Role.USER));
        when(postService.createPost(eq("owner@mail.com"), any()))
                .thenReturn(new Post(owner, "New title", "New body"));

        mvc.post()
                .uri("/api/posts")
                .with(jwt().jwt(jwt -> jwt.subject("owner@mail.com")))
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
    void updatePost_returnsOk() {
        when(postService.updatePost(eq(1L), eq("owner@mail.com"), any()))
                .thenReturn(new Post("Updated", "Updated body"));

        mvc.put()
                .uri("/api/posts/1")
                .with(jwt().jwt(jwt -> jwt.subject("owner@mail.com")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "title": "Updated",
                          "body": "Updated body"
                        }
                        """)
                .assertThat()
                .hasStatusOk();
    }

    @Test
    void deletePost_returns204() {
        doNothing().when(postService).deletePost(eq(1L), eq("owner@mail.com"));

        mvc.delete()
                .uri("/api/posts/1")
                .with(jwt().jwt(jwt -> jwt.subject("owner@mail.com")))
                .assertThat()
                .hasStatus(204);
    }

    @Test
    void getCommentsByPostId_returnsComments() {
        User user = new User("mail@example.com",  "pwd", Set.of(Role.USER));
        Comment c = new Comment();
        c.setContent("Hello");
        c.setOwner(user);
        CommentResponse commentResponse = new CommentResponse(c.getId(), c.getContent(), c.getOwner().getEmail(), c.getCreatedAt(), c.getUpdatedAt());

        when(commentService.getCommentsByPostId(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(commentResponse)));

        mvc.get()
                .uri("/api/posts/1/comments")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$.content[0].content").isEqualTo("Hello");
    }
}