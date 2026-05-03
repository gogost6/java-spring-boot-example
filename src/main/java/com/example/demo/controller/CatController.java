package com.example.demo.controller;

import com.example.demo.service.CatService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class CatController {
    private final CatService catService;

    public CatController(CatService catService) {
        this.catService = catService;
    }


    @GetMapping("/api/random-cat")
    public ResponseEntity<byte[]> getCat() {
        ResponseEntity<byte[]> response = catService.getRandomCatImage();

        return ResponseEntity
                .status(response.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE,
                        Objects.requireNonNull(response.getHeaders().getContentType()).toString())
                .body(response.getBody());
    }

    @GetMapping("/api/random-cat-by-tag/{tag}")
    public ResponseEntity<byte[]> getRandomCatByTag(@PathVariable String tag) {
        ResponseEntity<byte[]> response = catService.getRandomCatByTag(tag);

        return ResponseEntity
                .status(response.getStatusCode())
                .contentType(Objects.requireNonNull(response.getHeaders().getContentType()))
                .body(response.getBody());
    }

    @GetMapping("/api/random-cat-gif")
    public ResponseEntity<byte[]> getRandomCatGif() {
        ResponseEntity<byte[]> response = catService.getRandomCatGif();

        return ResponseEntity
                .status(response.getStatusCode())
                .contentType(response.getHeaders().getContentType())
                .body(response.getBody());
    }
}
