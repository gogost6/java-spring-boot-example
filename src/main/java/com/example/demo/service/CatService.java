package com.example.demo.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CatService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiUrl = "https://cataas.com";

    private ResponseEntity<byte[]> fetchImage(String path) {
        HttpHeaders headers = new HttpHeaders();

        // catass.com needs accept params otherwise it returns a JSON!
        headers.setAccept(List.of(MediaType.ALL));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        return restTemplate.exchange(
                apiUrl + path,
                HttpMethod.GET,
                entity,
                byte[].class
        );
    }

    public ResponseEntity<byte[]> getRandomCatImage() {
        return fetchImage("/cat");
    }

    public ResponseEntity<byte[]> getRandomCatByTag(String tag) {
        String[] tags = restTemplate.getForObject(apiUrl + "/api/tags", String[].class);

        if (tags == null || tags.length == 0) {
            return ResponseEntity.notFound().build();
        }

        boolean exists = false;

        for (String s : tags) {
            if (s.equals(tag)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        return fetchImage("/cat/" + tag);
    }

    public ResponseEntity<byte[]> getRandomCatGif() {
        return fetchImage("/cat/gif");
    }
}