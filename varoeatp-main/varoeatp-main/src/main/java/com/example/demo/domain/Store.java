package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity 
public class Store {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String storeName; 
    private String address;   
    private Double lat;       
    private Double lng;
    
    // 👇 [추가] 2개 필드 추가
    private String phoneNumber; // 전화번호
    private boolean isOpen;     // 영업 여부

    // [수정] 1. 빈 생성자
    public Store() {
    }
    
    // [수정] 2. 매장 데이터를 받기 위한 생성자 (6개 항목)
    public Store(String storeName, String address, Double lat, Double lng, String phoneNumber, boolean isOpen) {
        this.storeName = storeName;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.phoneNumber = phoneNumber;
        this.isOpen = isOpen;
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
    // 👇 [추가] 2개 Getter 추가
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public boolean getIsOpen() {
        return isOpen;
    }
    
}