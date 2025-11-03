package com.example.demo.auth; // AuthController와 같은 패키지에 생성, 마이페이지,회원정보 수정, 회원탈퇴 등을 담당

import com.example.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;

@Controller
@RequestMapping("/user") // '/user'로 시작하는 모든 요청을 이 컨트롤러가 처리
public class UserController {

    private final UserService userService;

    // 생성자를 통한 의존성 주입
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 마이페이지 화면을 보여주는 메소드
     */
    @GetMapping("/mypage")
    public String myPage(Model model) {
        // 현재 로그인한 사용자의 정보를 가져옴
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();

        // 사용자 정보를 모델에 추가하여 View로 전달
        model.addAttribute("userEmail", userEmail);

        return "mypage"; // mypage.html 파일을 렌더링 -> 이 부분은 SPA에서는 사용되지 않지만 유지합니다.
    }

    /**
     * 회원 탈퇴 처리 (비밀번호 확인 로직 추가)
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

                if (password == null || password.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("비밀번호를 입력해주세요.");
                }

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
}