package com.example.demo.service;

import java.time.Duration;
import java.util.List;

import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.config.CorrelationIdFilter;

@Service
public class CatService {

    private final RestTemplate restTemplate;
    private final CatTagsService catTagsService;

    public CatService(CatTagsService catTagsService) {
        this.catTagsService = catTagsService;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restTemplate = new RestTemplate(factory);
    }

    private ResponseEntity<byte[]> fetchImage(String path) {
        HttpHeaders headers = new HttpHeaders();

        // catass.com needs accept params otherwise it returns a JSON!
        headers.setAccept(List.of(MediaType.ALL));

        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId != null) {
            headers.set(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);
        }

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        String apiUrl = "https://cataas.com";
        return restTemplate.exchange(
                apiUrl + path,
                HttpMethod.GET,
                entity,
                byte[].class);
    }

    public ResponseEntity<byte[]> getRandomCatImage() {
        return fetchImage("/cat");
    }

    public ResponseEntity<byte[]> getRandomCatByTag(String tag) {
        String[] tags = catTagsService.fetchAllTags();

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