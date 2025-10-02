package com.example.demo.service;

import com.example.demo.auth.RegisterRequest;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.auth.LoginRequest; // LoginRequest import 추가
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    // SecurityConfig의 passwordEncoder 빈을 직접 주입받습니다.
    private final BCryptPasswordEncoder passwordEncoder; 

    // 생성자 주입 (순환 참조 문제 해결)
    // Spring이 UserRepository와 BCryptPasswordEncoder 빈을 찾아서 여기에 넣어줍니다.
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // --- 회원가입 로직 ---
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()) != null) {
            throw new RuntimeException("Username already exists.");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = new User();
        // 참고: DB 컬럼명에 맞게 user_name 대신 username 필드를 사용합니다.
        // User 엔티티의 @Column(name="user_name") 설정이 이 문제를 해결합니다.
        newUser.setUsername(request.getUsername()); 
        newUser.setPassword(encodedPassword);
        newUser.setEmail(request.getEmail());

        return userRepository.save(newUser);
    }
    
    // --- Spring Security가 사용자 정보를 로드할 때 호출됨 ---
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            new ArrayList<>()
        );
    }
    
    // --- 로그인 처리 로직 ---
    public String login(String username, String rawPassword) {
        UserDetails userDetails = loadUserByUsername(username);
        
        // 데이터베이스의 암호화된 비밀번호와 요청된 비밀번호를 비교
        if (passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            return "SUCCESS";
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
