package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import jakarta.persistence.Column; // 👈 1. 이 import 구문 (회색이어도 정상)

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // 👈 (해결됨) 이것은 기본 키
    private Long id;

    // 👇 1. 이 어노테이션을 username 필드 위에 추가하세요.
    @Column(name = "user_name")
    private String username;

    private String password;
    private String nickname;
    private String email;
    private String role;
    private String provider;
    private String phoneNumber;
    private String profileImageUrl; 
    private String preferredFood;   
}