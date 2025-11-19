package com.example.demo.dto;

import com.example.demo.domain.FoodType;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; 
import lombok.AllArgsConstructor; 

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor

public class UserProfileUpdateRequest {
    private String nickname;
    private List<FoodType> preferredFoods; // 수정할 선호 음식 목록
}