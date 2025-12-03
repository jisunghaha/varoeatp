package com.example.demo.controller;

import com.example.demo.domain.Review;
import com.example.demo.domain.Store;
import com.example.demo.domain.User;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.StoreRepository;
import com.example.demo.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    private String getEmailFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            String email = oauth2User.getAttribute("email");
            if (email == null) {
                Map<String, Object> kakaoAccount = oauth2User.getAttribute("kakao_account");
                if (kakaoAccount != null) {
                    email = (String) kakaoAccount.get("email");
                }
            }
            return email;
        } else if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        List<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc();
        List<ReviewDto> dtos = reviews.stream().map(ReviewDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping
    public ResponseEntity<String> createReview(@RequestBody ReviewRequestDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromAuthentication(authentication);

        if (email == null || email.equals("anonymousUser")) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Store store = null;
        if (request.getStoreId() != null) {
            store = storeRepository.findById(request.getStoreId()).orElse(null);
        }

        Review review = new Review(user, store, request.getContent(), request.getImageUrl());
        reviewRepository.save(review);

        return ResponseEntity.ok("Review created");
    }

    @Data
    static class ReviewRequestDto {
        private Long storeId;
        private String content;
        private String imageUrl;
    }

    @Data
    static class ReviewDto {
        private Long id;
        private String userName;
        private String storeName;
        private String content;
        private String imageUrl;
        private String createdAt;

        public ReviewDto(Review review) {
            this.id = review.getId();
            this.userName = review.getUser().getNickname() != null ? review.getUser().getNickname()
                    : review.getUser().getEmail();
            this.storeName = review.getStore() != null ? review.getStore().getStoreName() : "일반";
            this.content = review.getContent();
            this.imageUrl = review.getImageUrl();
            this.createdAt = review.getCreatedAt().toString();
        }
    }
}
