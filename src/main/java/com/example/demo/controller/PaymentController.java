package com.example.demo.controller;

import com.example.demo.service.PortOneService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PortOneService portOneService;

    @PostMapping("/verify/{impUid}")
    public ResponseEntity<?> verifyPayment(@PathVariable String impUid, @RequestBody Map<String, Object> payload) {
        try {
            String token = portOneService.getToken();
            JsonNode paymentData = portOneService.getPaymentData(impUid, token);

            int paidAmount = paymentData.path("amount").asInt();
            String status = paymentData.path("status").asText();

            // 프론트엔드에서 보낸 예상 금액과 실제 결제 금액 비교
            int expectedAmount = (int) payload.get("amount");

            if (expectedAmount == paidAmount && "paid".equals(status)) {
                return ResponseEntity
                        .ok(Map.of("status", "success", "message", "결제 검증 성공", "paymentData", paymentData));
            } else {
                return ResponseEntity.badRequest().body(Map.of("status", "fail", "message", "결제 금액 불일치 또는 결제 미완료"));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}
