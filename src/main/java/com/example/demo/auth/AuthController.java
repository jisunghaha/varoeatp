package com.example.demo.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.User;
import com.example.demo.dto.UserResponseDto; // 방금 만든 응답용 DTO
import com.example.demo.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService; // 생성자 주입 방식으로 변경
    public AuthController(UserService userService) {
        this.userService = userService;
}
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) { // 와일드카드 <?> 사용
        try {
            User registeredUser = userService.registerUser(request);
            // 비밀번호가 제외된 DTO를 만들어서 응답으로 보냄
            UserResponseDto responseDto = new UserResponseDto(registeredUser);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            // 실패 시 오류 메시지를 응답 본문에 담아 보냄
            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());
        }
    }
}