package com.example.demo.auth;

import com.example.demo.dto.ReservationResponse; // 👈 추가된 import
import com.example.demo.dto.UserProfileUpdateRequest;
import com.example.demo.dto.UserProfileResponse;
import com.example.demo.service.ReservationService; // 👈 추가된 import
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ReservationService reservationService; // 👈 필드 추가

    // 👈 생성자 수정 (ReservationService 주입)
    public UserController(UserService userService, ReservationService reservationService) {
        this.userService = userService;
        this.reservationService = reservationService;
    }

    /**
     * 마이페이지 정보 조회
     */
    @GetMapping("/mypage")
    public ResponseEntity<?> myPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            UserProfileResponse profile = userService.getUserProfile(auth.getName());
            return ResponseEntity.ok(profile);
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("email", auth.getName()));
        }
    }

    /**
     * 👇 [추가됨] 나의 예약 내역 조회 API
     */
    @GetMapping("/reservations")
    public ResponseEntity<?> getMyReservations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            // 로그인된 ID로 예약 내역 조회
            List<ReservationResponse> reservations = reservationService.getReservationsByUser(auth.getName());
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("예약 내역 조회 실패: " + e.getMessage());
        }
    }

    /**
     * 프로필 수정
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            userService.updateUserProfile(auth.getName(), request);
            return ResponseEntity.ok(Map.of("message", "프로필이 업데이트 되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * 회원 탈퇴
     */
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(HttpServletRequest request,
                                           HttpServletResponse response,
                                           @RequestBody Map<String, String> payload) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            try {
                String email = auth.getName();
                String password = payload.get("password");

                if (password == null || password.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("비밀번호를 입력해주세요.");
                }

                userService.deleteUser(email, password);

                new SecurityContextLogoutHandler().logout(request, response, auth);
                return ResponseEntity.ok("회원 탈퇴 성공");

            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
    }
}