package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store; // Optional: Review can be for a specific store or general

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl; // Base64 string or URL

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "rating")
    private int rating;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    private Reservation reservation;

    public Review(User user, Store store, String content, String imageUrl, int rating, Reservation reservation) {
        this.user = user;
        this.store = store;
        this.content = content;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.reservation = reservation;
        this.createdAt = LocalDateTime.now();
    }
}
