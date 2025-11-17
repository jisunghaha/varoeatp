package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean; // import 추가
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // import 추가
import org.springframework.security.crypto.password.PasswordEncoder; // import 추가

@SpringBootApplication
public class BaroeatpApplication {

    public static void main(String[] args) {
        SpringApplication.run(BaroeatpApplication.class, args);
    }

}