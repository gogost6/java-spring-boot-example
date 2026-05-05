package com.example.demo.controller;

import com.example.demo.service.CatService;
import com.example.demo.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final PostService postService;
    private final CatService catService;

    public PageController(PostService postService, CatService catService) {
        this.postService = postService;
        this.catService = catService;
    }

    @GetMapping("/posts-page")
    public String postsPage(Model model) {
        model.addAttribute("posts", postService.getAllPostsWithComments());
        return "posts";
    }

    @GetMapping("/cat-page")
    public String catPage() {
        return "cat";
    }

    @GetMapping("/cat-image")
    public ResponseEntity<byte[]> getCatImage() {
        return catService.getRandomCatImage();
    }
}