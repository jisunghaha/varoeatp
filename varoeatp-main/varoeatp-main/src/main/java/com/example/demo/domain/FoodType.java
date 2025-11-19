package com.example.demo.domain;

public enum FoodType {
    KOREAN("한식"),
    CHINESE("중식"),
    JAPANESE("일식"),
    WESTERN("양식"),
    SNACK("분식"),
    CHICKEN("치킨"),
    PIZZA("피자"),
    FASTFOOD("패스트푸드"),
    DESERT("디저트"),
    JOKBAL_BOSSAM("족발/보쌈"),
    ASIAN("아시안"),        
    MEXICAN("멕시칸"),     
    LUNCHBOX("도시락/죽"),
    SALAD("샐러드/샌드위치"),
    CAFE("카페/음료"),
    BAKERY("베이커리"),
    MEAT("고기/구이"),
    BAR("술집/호프");

    private final String description;

    FoodType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}