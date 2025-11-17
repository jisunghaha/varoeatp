package com.example.demo.service;

import com.example.demo.auth.RegisterRequest;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder; // 👈 import 문이 이것으로 변경되었습니다.
import org.springframework.stereotype.Service;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder; // 👈 1. 이 import가 있는지 확인
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor; // 👈 2. 이 import가 있는지 확인
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
    public void deleteUser(String email, String password) {
        // 1. 이메일로 사용자를 찾습니다. (Optional이 아닌 User 반환)
        User user = userRepository.findByEmail(email);

        // 2. 사용자가 없는 경우(null) 오류 발생
        if (user == null) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        // 3. 입력된 비밀번호와 DB의 비밀번호가 일치하는지 확인합니다.
        if (!passwordEncoder.matches(password, user.getPassword())) {
            // 비밀번호가 일치하지 않으면 오류 발생
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }
        
        // 4. 비밀번호가 일치하면 사용자를 삭제합니다.
        userRepository.delete(user);
    }
}