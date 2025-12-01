package com.example.demo.controller;

import com.example.demo.dto.AvailableTimeResponse;
import com.example.demo.dto.ReservationRequest;
import com.example.demo.dto.TableOptionResponse;
import com.example.demo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // 👈 import 변경
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    /**
     * 날짜별 가용 시간 조회 API
     */
    @GetMapping("/times")
    public ResponseEntity<List<AvailableTimeResponse>> getAvailableTimes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reservationService.getAvailableTimes(date));
    }

    /**
     * 테이블 옵션 조회 API
     */
    @GetMapping("/tables")
    public ResponseEntity<List<TableOptionResponse>> getAvailableTableOptions(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam int partySize) {
        return ResponseEntity.ok(reservationService.getAvailableTableOptions(date, time, partySize));
    }

    /**
     * 예약 생성 API (수정됨)
     */
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody ReservationRequest request,
            Authentication authentication) { // 👈 [핵심 수정] OAuth2User 대신 Authentication 사용

        // 1. 로그인 여부 확인
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        // 2. 사용자 ID 가져오기
        // Authentication.getName()은 소셜 로그인이든 일반 로그인이든 상관없이
        // "식별자(일반: 이메일, 소셜: 카카오ID)"를 반환해줍니다.
        String identifier = authentication.getName();

        try {
            // 서비스로 식별자를 넘깁니다. (UserService가 알아서 처리함)
            reservationService.createReservation(request, identifier);
            return ResponseEntity.ok(Map.of("message", "예약이 완료되었습니다."));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}