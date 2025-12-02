package com.example.demo.auth;

import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController // 👈 @Controller가 아닌 @RestController 입니다.
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 마이페이지 (사용자 정보 조회)
     */
    @GetMapping("/mypage")
    public ResponseEntity<?> myPage() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        if (userEmail == null || userEmail.equals("anonymousUser")) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요합니다."));
        }

        try {
            User user = userService.getUserByEmail(userEmail);

            return ResponseEntity.ok(Map.of(
                    "email", user.getEmail(),
                    "nickname", user.getNickname() != null ? user.getNickname() : "",
                    "preferredFood", user.getPreferredFood() != null ? user.getPreferredFood() : ""));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("message", "사용자 정보를 찾을 수 없습니다."));
        }
    }

    /**
     * 회원 탈퇴 처리
     */
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdraw(HttpServletRequest request,
            HttpServletResponse response,
            @RequestBody Map<String, String> payload) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null) {
            try {
                String email = auth.getName();
                String password = payload.get("password");

                // 서비스의 deleteUser 메소드 호출 (비밀번호와 함께)
                userService.deleteUser(email, password);

                // 세션을 무효화하고 로그아웃 처리
                new SecurityContextLogoutHandler().logout(request, response, auth);

                return ResponseEntity.ok("회원 탈퇴 성공");

            } catch (RuntimeException e) {
                // 비밀번호 불일치 등 서비스 레이어에서 발생한 예외 처리
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }
        return ResponseEntity.status(401).body("인증되지 않은 사용자입니다.");
    }

    /**
     * 프로필 업데이트 (닉네임, 선호 음식)
     */
    @PutMapping("/profile")
    public ResponseEntity<String> updateProfile(@RequestBody com.example.demo.dto.UserUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName().equals("anonymousUser")) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            userService.updateProfile(auth.getName(), request.getNickname(), request.getPreferredFood());
            return ResponseEntity.ok("프로필이 업데이트되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("프로필 업데이트 실패: " + e.getMessage());
        }
    }
}