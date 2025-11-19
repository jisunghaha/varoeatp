package com.example.demo.controller;

import com.example.demo.dto.AvailableTimeResponse;
import com.example.demo.dto.ReservationRequest;
import com.example.demo.dto.TableOptionResponse;
import com.example.demo.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm") LocalTime time,
            @RequestParam int partySize) {
        return ResponseEntity.ok(reservationService.getAvailableTableOptions(date, time, partySize));
    }

    /**
     * 예약 생성 API
     */
    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestBody ReservationRequest request,
            @AuthenticationPrincipal OAuth2User oauthUser) { // OAuth2 로그인 사용자 기준
        
        if (oauthUser == null) {
            // TODO: 일반 로그인 사용자인 경우 처리 (SecurityContext에서 가져오기)
            // 임시로 OAuth2만 처리. 일반 로그인은 이 프로젝트에 구현되지 않은 것으로 보임.
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }
        
        Map<String, Object> attributes = oauthUser.getAttributes();
        String email = (String) attributes.get("email"); 
        
        if (email == null) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response"); // 네이버 등
            if(response != null) {
                email = (String) response.get("email");
            }
        }
         
        if (email == null) {
             // 카카오 등 'kakao_account' 안에 정보가 있는 경우
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
            }
        }

        if (email == null) {
            return ResponseEntity.status(400).body(Map.of("message", "사용자 이메일을 가져올 수 없습니다."));
        }

        try {
            reservationService.createReservation(request, email);
            return ResponseEntity.ok(Map.of("message", "예약이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}