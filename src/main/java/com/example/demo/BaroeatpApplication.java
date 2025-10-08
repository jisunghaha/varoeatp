package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // import 추가
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // import 추가
import org.springframework.security.crypto.password.PasswordEncoder; // import 추가

@SpringBootApplication
// @Import(SecurityConfig.class) // 이 줄은 이제 주석 처리하거나 삭제합니다.
public class BaroeatpApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaroeatpApplication.class, args);
    }

    // 👇 여기에 PasswordEncoder Bean을 직접 추가합니다!
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}