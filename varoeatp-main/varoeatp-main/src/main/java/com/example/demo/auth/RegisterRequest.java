package com.example.demo.auth;

import lombok.Getter; // 👈 1. 이 import 구문을 추가하세요.
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Setter
@Getter // 👈 2. 이 어노테이션을 추가하세요.
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String username;
    private String password;
    private String nickname;
    private String email;
}