package com.georgistoilkov.catify.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CatTagsService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Cacheable("catTags")
    public String[] fetchAllTags() {
        String apiUrl = "https://cataas.com";
        return restTemplate.getForObject(apiUrl + "/api/tags", String[].class);
    }
}
