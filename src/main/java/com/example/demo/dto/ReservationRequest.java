package com.example.demo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Setter 추가 필요

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor // 기본 생성자 추가
public class ReservationRequest {
    private LocalDate date;
    private String time;
    private int partySize;
    private Long tableId;

    private List<ReservationItemRequest> items;

    @Getter @Setter
    @NoArgsConstructor
    public static class ReservationItemRequest {
        private Long productId;
        private int count;
    }
}