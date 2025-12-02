package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "store_tables")
@Getter
@Setter
@NoArgsConstructor
public class StoreTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "table_id")
    private Long id;

    private String name;
    private String description;
    private int capacityMin;
    private int capacityMax;
    private int additionalPrice;
    // private int totalCount; // 제거됨

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
}