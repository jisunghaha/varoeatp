package com.example.demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortOneService {

    @Value("${portone.api.key}")
    private String apiKey;

    @Value("${portone.api.secret}")
    private String apiSecret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 포트원 API 토큰 발급
    public String getToken() {
        String url = "https://api.iamport.kr/users/getToken";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("imp_key", apiKey);
        body.put("imp_secret", apiSecret);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("code").asInt() == 0) {
                return root.path("response").path("access_token").asText();
            } else {
                throw new RuntimeException("토큰 발급 실패: " + root.path("message").asText());
            }
        } catch (Exception e) {
            throw new RuntimeException("포트원 API 호출 중 오류 발생", e);
        }
    }

    // 결제 정보 조회
    public JsonNode getPaymentData(String impUid, String token) {
        String url = "https://api.iamport.kr/payments/" + impUid;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("code").asInt() == 0) {
                return root.path("response");
            } else {
                throw new RuntimeException("결제 정보 조회 실패: " + root.path("message").asText());
            }
        } catch (Exception e) {
            throw new RuntimeException("포트원 결제 조회 중 오류 발생", e);
        }
    }
}
