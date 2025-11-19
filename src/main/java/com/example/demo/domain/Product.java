package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity 
public class Product {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // 상품 이름
    private int price;          // 상품 가격
    private String description; // 상품 상세 설명
    private Long storeId;       // 속한 매장의 ID (가장 중요)
    private String category;    // 카테고리 (예: 국밥, 사이드 메뉴)
    private String imageUrl;    // 상품 이미지 URL

    // JPA 사용을 위한 기본 생성자 (필수)
    public Product() {
    }

    // 데이터 삽입을 위한 생성자
    public Product(String name, int price, String description, Long storeId, String category, String imageUrl) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.storeId = storeId;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    // --- Getter ---

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getDescription() {
        return description;
    }

    public Long getStoreId() {
        return storeId;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}