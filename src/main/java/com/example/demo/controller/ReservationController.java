package com.example.demo.controller;

import com.example.demo.dto.AvailableTimeResponse;
import com.example.demo.dto.ReservationRequest;
import com.example.demo.dto.TableOptionResponse;
import com.example.demo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations") // API 기본 경로
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
            @RequestParam Long storeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam int partySize) {
        return ResponseEntity.ok(reservationService.getAvailableTableOptions(storeId, date, time, partySize));
    }

    /**
     * 예약 생성 API
     */
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody ReservationRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        String userIdentifier = getUserIdentifier(auth);

        try {
            reservationService.createReservation(request, userIdentifier);
            return ResponseEntity.ok(Map.of("message", "예약이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReservation(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        String userIdentifier = getUserIdentifier(auth);
        try {
            reservationService.cancelReservation(id, userIdentifier);
            return ResponseEntity.ok(Map.of("message", "예약이 취소되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifyReservation(@PathVariable Long id, @RequestBody ReservationRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        String userIdentifier = getUserIdentifier(auth);
        try {
            reservationService.modifyReservation(id, request, userIdentifier);
            return ResponseEntity.ok(Map.of("message", "예약이 변경되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // 특정 매장의 테이블 목록 조회
    @GetMapping("/store/{storeId}/tables")
    public ResponseEntity<List<com.example.demo.domain.StoreTable>> getTablesByStore(@PathVariable Long storeId) {
        return ResponseEntity.ok(reservationService.getTablesByStore(storeId));
    }

    // 내 예약 내역 조회
    @GetMapping("/my")
    public ResponseEntity<List<com.example.demo.dto.ReservationResponse>> getMyReservations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }

        String userIdentifier = getUserIdentifier(auth);
        return ResponseEntity.ok(reservationService.getReservationsByUser(userIdentifier));
    }

    private String getUserIdentifier(Authentication auth) {
        if (auth.getPrincipal() instanceof OAuth2User) {
            OAuth2User oauthUser = (OAuth2User) auth.getPrincipal();
            Map<String, Object> attributes = oauthUser.getAttributes();

            // Kakao ID 추출 (CustomOAuth2UserService 로직과 일치시켜야 함)
            Object id = attributes.get("id");
            if (id != null) {
                return id.toString() + "_kakao";
            }
            // 다른 OAuth2 제공자 처리 필요 시 추가
        }
        return auth.getName(); // 일반 로그인 (이메일) 또는 OAuth2 기본 name
    }
}