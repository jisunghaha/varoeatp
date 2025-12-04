package com.example.demo.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=";

    public String getChatResponse(String prompt) {
        String url = API_URL + apiKey.trim();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        GeminiRequest request = new GeminiRequest(prompt);
        HttpEntity<GeminiRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<GeminiResponse> response = restTemplate.postForEntity(url, entity, GeminiResponse.class);
            GeminiResponse body = response.getBody();
            if (body != null && !body.getCandidates().isEmpty()) {
                return body.getCandidates().get(0).getContent().getParts().get(0).getText();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "죄송합니다. 현재 AI 응답을 가져올 수 없습니다.";
        }
        return "응답이 없습니다.";
    }

    @Data
    static class GeminiRequest {
        private List<Content> contents;

        public GeminiRequest(String text) {
            this.contents = Collections.singletonList(new Content(Collections.singletonList(new Part(text))));
        }

        @Data
        static class Content {
            private List<Part> parts;

            public Content(List<Part> parts) {
                this.parts = parts;
            }
        }

        @Data
        static class Part {
            private String text;

            public Part(String text) {
                this.text = text;
            }
        }
    }

    @Data
    static class GeminiResponse {
        private List<Candidate> candidates;

        @Data
        static class Candidate {
            private Content content;
        }

        @Data
        static class Content {
            private List<Part> parts;
        }

        @Data
        static class Part {
            private String text;
        }
    }
}
