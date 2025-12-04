package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "baro_reservations")
@Getter
@Setter
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @Column(name = "reservation_date")
    private LocalDate reservationDate;

    @Column(name = "reservation_time")
    private LocalTime reservationTime;

    @Column(name = "party_size")
    private int partySize;

    @Column(name = "num_of_people")
    private int numOfPeople;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_id")
    private StoreTable storeTable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @Column(name = "payment_method")
    private String paymentMethod; // "ON_SITE", "CARD"

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "imp_uid")
    private String impUid;

    @Column(name = "payment_status")
    private String paymentStatus; // "PAID", "PENDING", "CANCELLED"

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationMenu> reservationMenus = new ArrayList<>();

    public void addReservationMenu(ReservationMenu reservationMenu) {
        reservationMenus.add(reservationMenu);
        reservationMenu.setReservation(this);
    }
}