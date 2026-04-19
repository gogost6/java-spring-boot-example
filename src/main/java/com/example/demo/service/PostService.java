package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PostService {

    private final RestTemplate restTemplate = new RestTemplate();

    public String getPosts() {
        String url = "https://jsonplaceholder.typicode.com/posts?_limit=5";
        return restTemplate.getForObject(url, String.class);
    }
}