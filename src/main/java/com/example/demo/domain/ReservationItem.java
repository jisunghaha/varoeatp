package com.example.demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore; // 👈 import 필수!
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString; // 👈 import

@Entity
@Table(name = "baro_reservation_items")
@Getter @Setter
@NoArgsConstructor
public class ReservationItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 예약 정보 (무한 루프의 원인!)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    @JsonIgnore        // 👈 1. JSON 변환 시 무시 (가장 중요)
    @ToString.Exclude  // 👈 2. 로그 출력 시 무시
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int count;

    // 생성자
    public ReservationItem(Reservation reservation, Product product, int count) {
        this.reservation = reservation;
        this.product = product;
        this.count = count;
    }
}