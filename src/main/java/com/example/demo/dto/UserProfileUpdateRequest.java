package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
public class UserProfileUpdateRequest {
    private String nickname;
    private List<String> preferredFoods; // 체크된 음식 목록 (예: ["KOREAN", "CHICKEN"])
}