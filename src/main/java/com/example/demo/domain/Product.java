package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "baro_product") // 👈 이름 변경!
@Getter
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    private String description;
    private Long storeId;
    private String category;
    private String imageUrl;

    public Product(String name, int price, String description, Long storeId, String category, String imageUrl) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.storeId = storeId;
        this.category = category;
        this.imageUrl = imageUrl;
    }
}