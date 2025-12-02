package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ReservationRequest {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String time; // "HH:mm" 형식의 문자열

    private int partySize;

    private Long tableId;

    private String paymentMethod; // "ON_SITE", "CARD"

    private String impUid; // 포트원 결제 고유 번호

    private List<MenuRequest> menus;

    @Getter
    @Setter
    public static class MenuRequest {
        private Long productId;
        private int quantity;
    }
}