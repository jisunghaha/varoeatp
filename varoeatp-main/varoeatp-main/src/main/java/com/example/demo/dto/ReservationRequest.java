package com.example.demo.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class ReservationRequest {
    private LocalDate date;
    private String time; // "HH:mm" 형식의 문자열
    private int partySize;
    private Long tableId;
}