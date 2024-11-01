// ImageGenerationController.java
package com.example.imagegeneration.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
public class ImageGenerationController {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, String>> generateImage(@RequestBody Map<String, String> requestData) {
        // 요청 데이터에서 카테고리 정보를 추출
        String style = requestData.get("style");
        String subject = requestData.get("subject");
        String emotion = requestData.get("emotion");
        String background = requestData.get("background");
        String message = requestData.get("message");

        // DALL-E 3 프롬프트 생성
        String prompt = String.format("Style: %s, Subject: %s, Emotion: %s, Background: %s. %s",
            style, subject, emotion, background, message);

        // OpenAI API에 요청을 보내기 위한 헤더와 본문 설정
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "https://api.openai.com/v1/images/generations";

        Map<String, Object> payload = new HashMap<>();
        payload.put("prompt", prompt);
        payload.put("n", 1); // 한 개의 이미지만 생성
        payload.put("size", "1024x1024"); // 이미지 크기 설정

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + openAiApiKey);
        headers.put("Content-Type", "application/json");

        try {
            // API 요청 및 응답 처리
            ResponseEntity<Map> response = restTemplate.postForEntity(
                UriComponentsBuilder.fromHttpUrl(apiUrl).toUriString(),
                new HttpEntity<>(payload, headers),
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String imageUrl = (String) ((Map<String, Object>) response.getBody().get("data")).get(0).get("url");

                Map<String, String> responseData = new HashMap<>();
                responseData.put("imageUrl", imageUrl);
                return ResponseEntity.ok(responseData);
            } else {
                return ResponseEntity.status(response.getStatusCode()).body(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
