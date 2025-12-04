package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/mypage")
    public ResponseEntity<?> getMyPage(@AuthenticationPrincipal Object principal) {
        if (principal == null)
            return ResponseEntity.status(401).body("로그인이 필요합니다.");

        String username = null;
        if (principal instanceof OAuth2User) {
            // Kakao Login
            String kakaoId = ((OAuth2User) principal).getName();
            username = kakaoId + "_kakao";
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // Form Login
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof Principal) {
            username = ((Principal) principal).getName();
        }

        try {
            User user = userService.getUserDetails(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            // Try without _kakao if failed (just in case)
            try {
                if (principal instanceof OAuth2User) {
                    User user = userService.getUserDetails(((OAuth2User) principal).getName());
                    return ResponseEntity.ok(user);
                }
            } catch (Exception ex) {
            }
            return ResponseEntity.badRequest().body("사용자 정보를 찾을 수 없습니다.");
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal Object principal,
            @RequestBody Map<String, String> body) {
        if (principal == null)
            return ResponseEntity.status(401).body("로그인이 필요합니다.");

        String username = null;
        if (principal instanceof OAuth2User) {
            username = ((OAuth2User) principal).getName() + "_kakao";
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof Principal) {
            username = ((Principal) principal).getName();
        }

        try {
            userService.updateProfileByUsername(username, body.get("nickname"), body.get("preferredFood"));
            return ResponseEntity.ok("프로필이 수정되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("프로필 수정 실패: " + e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@AuthenticationPrincipal Object principal,
            @RequestBody Map<String, String> body) {
        if (principal == null)
            return ResponseEntity.status(401).body("로그인이 필요합니다.");

        String username = null;
        boolean isOAuth = false;
        if (principal instanceof OAuth2User) {
            username = ((OAuth2User) principal).getName() + "_kakao";
            isOAuth = true;
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
        } else if (principal instanceof Principal) {
            username = ((Principal) principal).getName();
        }

        if (username == null) {
            return ResponseEntity.badRequest().body("사용자 정보를 확인할 수 없습니다.");
        }

        try {
            // For form users, check password
            if (!isOAuth) {
                String password = body.get("password");
                if (password == null || password.isEmpty()) {
                    return ResponseEntity.badRequest().body("비밀번호를 입력해주세요.");
                }
                User user = userService.getUserDetails(username);
                userService.deleteUser(user.getEmail(), password);
            } else {
                // For OAuth users, just delete by username
                userService.deleteUserByUsername(username);
            }

            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("회원 탈퇴 실패: " + e.getMessage());
        }
    }
}
