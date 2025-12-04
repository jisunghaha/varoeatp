package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "baro_reservation_menus")
@Getter
@Setter
@NoArgsConstructor
public class ReservationMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    public ReservationMenu(Reservation reservation, Product product, int quantity) {
        this.reservation = reservation;
        this.product = product;
        this.quantity = quantity;
    }
}
