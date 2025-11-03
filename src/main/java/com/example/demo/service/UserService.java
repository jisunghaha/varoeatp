package com.example.demo.service;

import com.example.demo.auth.RegisterRequest;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // 👈 import 문이 이것으로 변경되었습니다.
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 👈 BCryptPasswordEncoder에서 변경되었습니다.

    // 순환 참조 방지를 위한 생성자 주입
    // 👇 생성자 파라미터도 PasswordEncoder로 변경되었습니다.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- 회원가입 로직 ---
    public User registerUser(RegisterRequest request) {
        // 이메일 중복 검사
        if (userRepository.findByEmail(request.getEmail()) != null) {
            throw new RuntimeException("Email already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = new User();
        newUser.setUserName(request.getUserName());
        newUser.setEmail(request.getEmail());
        newUser.setPhoneNumber(request.getPhoneNumber());
        newUser.setPassword(encodedPassword);

        return userRepository.save(newUser);
    }

    // --- Spring Security가 사용자 정보를 로드할 때 호출됨 ---
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            new ArrayList<>()
        );
    }

    // --- 로그인 처리 로직 (이 부분은 SecurityConfig가 처리하지만, 직접 호출할 경우를 위해 유지) ---
    public String login(String username, String rawPassword) {
        // username(email)로 사용자 정보를 로드합니다.
        UserDetails userDetails = loadUserByUsername(username);

        if (passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            return "SUCCESS";
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }

    // --- 회원 탈퇴 로직 ---
    public void deleteUser(String email, String rawPassword) {
        // 이메일로 사용자 정보를 로드합니다.
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        // 입력된 비밀번호가 저장된 비밀번호와 일치하는지 확인합니다.
        if (passwordEncoder.matches(rawPassword, user.getPassword())) {
            userRepository.delete(user);
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}