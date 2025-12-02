package com.example.demo.auth;

import com.example.demo.domain.User;
import com.example.demo.dto.UserResponseDto;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User registeredUser = userService.registerUser(request);
            UserResponseDto responseDto = new UserResponseDto(registeredUser);
            return ResponseEntity.ok(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest,
            jakarta.servlet.http.HttpServletResponse httpResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            org.springframework.security.core.context.SecurityContext context = SecurityContextHolder
                    .createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Spring Security 6: Explicitly save context
            org.springframework.security.web.context.SecurityContextRepository securityContextRepository = new org.springframework.security.web.context.HttpSessionSecurityContextRepository();
            securityContextRepository.saveContext(context, httpRequest, httpResponse);

            return ResponseEntity.ok(authentication.getName());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Login failed: " + e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        return ResponseEntity.ok(authentication.getName());
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(jakarta.servlet.http.HttpServletRequest request) {
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("Logged out");
    }
}