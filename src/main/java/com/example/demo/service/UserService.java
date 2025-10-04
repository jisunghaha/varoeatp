package com.example.demo.service;

import com.example.demo.auth.RegisterRequest;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import com.example.demo.auth.LoginRequest; // 필요한 import 추가

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder; 

    // 순환 참조 방지를 위한 생성자 주입
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
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
        // **수정된 필드명과 Getter를 사용합니다.**
        // RegisterRequest에 있는 getUserName()과 setUserName() 매핑
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
    
    // --- 로그인 처리 로직 ---
    public String login(String username, String rawPassword) {
        // username(email)로 사용자 정보를 로드합니다.
        UserDetails userDetails = loadUserByUsername(username); 
        
        if (passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            return "SUCCESS";
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
