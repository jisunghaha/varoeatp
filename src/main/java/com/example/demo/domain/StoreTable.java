package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table; // 👈 1. import 추가
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_tables") // 👈 2. 이 줄을 추가
@Getter
@Setter
@NoArgsConstructor
public class StoreTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; 
    private String description; 
    private int capacityMin; 
    private int capacityMax; 
    private int additionalPrice; 
    private int totalCount; 
}