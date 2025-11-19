package com.example.demo.dto;

import com.example.demo.domain.FoodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private String email;
    private String nickname;
    private String profileImageUrl;
    private List<FoodType> preferredFoods; // 선호 음식 목록
}