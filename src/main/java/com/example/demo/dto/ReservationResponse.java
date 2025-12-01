package com.example.demo.dto;

import com.example.demo.domain.Reservation;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor
public class ReservationResponse {
    private Long id;
    private String storeName;
    private String reservationDate;
    private String reservationTime;
    private int partySize;

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        // StoreTable -> Store -> StoreName 순으로 접근
        this.storeName = reservation.getStoreTable().getStore().getStoreName();
        this.reservationDate = reservation.getReservationDate().toString();
        this.reservationTime = reservation.getReservationTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        this.partySize = reservation.getPartySize();
    }
}