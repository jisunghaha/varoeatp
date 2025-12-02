package com.example.demo.dto;

import com.example.demo.domain.Reservation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
public class ReservationResponse {

    private Long id;
    private String storeName;
    private String tableName;
    private String date;
    private String time;
    private int partySize;

    public ReservationResponse(Reservation reservation) {
        this.id = reservation.getId();
        this.date = reservation.getReservationDate().toString();
        this.time = reservation.getReservationTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        this.partySize = reservation.getPartySize();

        if (reservation.getStoreTable() != null) {
            this.tableName = reservation.getStoreTable().getName();
            if (reservation.getStoreTable().getStore() != null) {
                this.storeName = reservation.getStoreTable().getStore().getStoreName();
            }
        }
    }
}
