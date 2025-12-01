package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. 로그인 화면에서 입력한 이메일로 유저를 찾습니다.
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email));

        // 2. 찾아낸 유저 정보를 스프링 시큐리티가 이해할 수 있는 UserDetails 객체로 변환합니다.
        // (이 과정에서 DB에 저장된 암호화된 비밀번호가 스프링 시큐리티에 전달됩니다.)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // 인증 식별자로 이메일 사용
                .password(user.getPassword()) // DB에 저장된 암호화된 비밀번호
                .roles("USER") // 권한 설정 (기본값 USER)
                .build();
    }
}