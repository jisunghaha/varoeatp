package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_id")
    private Long id;

    @Column(name = "store_name")
    private String storeName;

    private String address;

    @Column(name = "latitude")
    private Double lat;

    @Column(name = "longitude")
    private Double lng;

    // 👇 [추가] 2개 필드 추가
    @Column(name = "phone_number")
    private String phoneNumber; // 전화번호

    @Column(name = "is_open")
    private Boolean isOpen; // 영업 여부 (Nullable)

    @Column(name = "category")
    private String category; // 매장 카테고리 (예: 한식, 양식, 주점)

    // [수정] 1. 빈 생성자
    public Store() {
    }

    // [수정] 2. 매장 데이터를 받기 위한 생성자 (7개 항목)
    public Store(String storeName, String address, Double lat, Double lng, String phoneNumber, Boolean isOpen,
            String category) {
        this.storeName = storeName;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.phoneNumber = phoneNumber;
        this.isOpen = isOpen;
        this.category = category;
    }

    // --- Getter (데이터를 읽는 '입구') ---

    public String getStoreName() {
        return storeName;
    }

    public String getAddress() {
        return address;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLng() {
        return lng;
    }

    public Long getId() {
        return id;
    }

    // 👇 [추가] 3개 Getter 추가
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Boolean getIsOpen() {
        return isOpen;
    }

    public String getCategory() {
        return category;
    }

}