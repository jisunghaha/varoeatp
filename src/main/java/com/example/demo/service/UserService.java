package com.example.demo.service;

import com.example.demo.auth.RegisterRequest;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // C(R)UD
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입 로직
     */
    @Transactional
    public User registerUser(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 사용자 이름입니다.");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .role("USER")
                .build();

        return userRepository.save(user);
    }


    @Transactional //
    public void deleteUser(String username, String password) {

        // 2. 사용자 이름으로 User 찾기
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username)) // 이메일로도 찾기
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 3. 비밀번호 확인 (일반 유저인 경우에만)
        if (user.getProvider() == null || user.getProvider().isEmpty()) {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }

        // 4. 사용자 삭제
        userRepository.delete(user);
    }
}