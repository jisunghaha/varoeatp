package com.example.demo.dto;

import com.example.demo.domain.StoreTable;
import lombok.Getter;

@Getter
public class TableOptionResponse {
    private Long id;
    private String storeName; // 식당 이름 추가
    private String name;
    private String capacity; // 예: "2-4명"
    private String description;
    private int price;
    private int availableCount; // 남은 좌석 수

    public TableOptionResponse(StoreTable table, int availableCount) {
        this.id = table.getId();
        this.storeName = table.getStore().getStoreName(); // 식당 이름 설정
        this.name = table.getName();
        this.capacity = table.getCapacityMin() + "-" + table.getCapacityMax() + "명";
        this.description = table.getDescription();
        this.price = table.getAdditionalPrice();
        this.availableCount = availableCount;
    }
}