package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.model.Post;


@Service
public class PostServiceExternal {

    private final RestTemplate restTemplate = new RestTemplate();

    public List<Post> getPosts() {
        String url = "https://jsonplaceholder.typicode.com/posts?_limit=5";
        Post[] posts = restTemplate.getForObject(url, Post[].class);
        return List.of(posts);
    }
}