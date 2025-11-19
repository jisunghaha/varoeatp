package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailableTimeResponse {
    private String time; // "HH:mm"
    private String status; // "available", "popular", "full"
}