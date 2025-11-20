package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "baro_store") // 👈 이름 변경!
@Getter
@NoArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeName;
    private String address;
    private Double lat;
    private Double lng;
    private String phoneNumber;
    private boolean isOpen;

    public Store(String storeName, String address, Double lat, Double lng, String phoneNumber, boolean isOpen) {
        this.storeName = storeName;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.phoneNumber = phoneNumber;
        this.isOpen = isOpen;
    }
}