package com.example.demo.dto;

import com.example.demo.domain.User;
import lombok.Getter;

// 비밀번호 등 민감한 정보를 제외하고 응답하기 위한 DTO
@Getter
public class UserResponseDto {
    private Long id;
    private String userName;
    private String email;
    private String phoneNumber;

    public UserResponseDto(User user) {
        this.id = user.getId(); // User 엔티티에 getId()가 있다고 가정
        this.userName = user.getUserName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
    }
}