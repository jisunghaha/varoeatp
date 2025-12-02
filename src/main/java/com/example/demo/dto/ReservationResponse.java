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
    private int totalPrice;
    private String paymentMethod;
    private String paymentTime;
    private java.util.List<MenuResponse> menus;

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

        this.menus = reservation.getReservationMenus().stream()
                .map(MenuResponse::new)
                .collect(java.util.stream.Collectors.toList());

        // Calculate total price (Table price + Menu prices)
        int tablePrice = (reservation.getStoreTable() != null) ? reservation.getStoreTable().getAdditionalPrice() : 0;
        int menuPrice = reservation.getReservationMenus().stream()
                .mapToInt(rm -> rm.getProduct().getPrice() * rm.getQuantity())
                .sum();
        this.totalPrice = tablePrice + menuPrice;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MenuResponse {
        private Long id;
        private String name;
        private int price;
        private int quantity;

        public MenuResponse(com.example.demo.domain.ReservationMenu rm) {
            this.id = rm.getProduct().getId();
            this.name = rm.getProduct().getName();
            this.price = rm.getProduct().getPrice();
            this.quantity = rm.getQuantity();
        }
    }
}
