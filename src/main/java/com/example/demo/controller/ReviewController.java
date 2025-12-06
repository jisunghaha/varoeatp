package com.example.demo.controller;

import com.example.demo.domain.Reservation;
import com.example.demo.domain.Review;
import com.example.demo.domain.Store;
import com.example.demo.domain.User;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.StoreRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final StoreRepository storeRepository;

    private String getUserIdentifier(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            // Kakao ID 추출 (CustomOAuth2UserService 로직과 일치)
            Object id = oauth2User.getAttribute("id");
            if (id != null) {
                return id.toString() + "_kakao";
            }
            return oauth2User.getName();
        } else if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else if (principal instanceof Principal) {
            return ((Principal) principal).getName();
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
        String username = getUserIdentifier(authentication);

        if (username == null || username.equals("anonymousUser")) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Store store = null;
        Reservation reservation = null;

        // 1. 예약 기반 리뷰
        if (request.getReservationId() != null) {
            reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> new RuntimeException("예약 정보를 찾을 수 없습니다."));

            if (!reservation.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("본인의 예약에 대해서만 리뷰를 작성할 수 있습니다.");
            }
            store = reservation.getStore();

        }
        // 2. 매장 직접 선택 리뷰 (예약 없이)
        else if (request.getStoreId() != null) {
            store = storeRepository.findById(request.getStoreId())
                    .orElseThrow(() -> new RuntimeException("매장 정보를 찾을 수 없습니다."));
        } else {
            return ResponseEntity.status(400).body("예약 ID 또는 매장 선택이 필요합니다.");
        }

        Review review = new Review(user, store, request.getContent(), request.getImageUrl(), request.getRating(),
                reservation);
        reviewRepository.save(review);

        return ResponseEntity.ok("Review created");
    }

    @Data
    static class ReviewRequestDto {
        private Long reservationId; // StoreId 대신 ReservationId 사용
        private Long storeId; // 매장 직접 선택 시 사용
        private String content;
        private String imageUrl;
        private int rating;
    }

    @Data
    static class ReviewDto {
        private Long id;
        private String userName;
        private String storeName;
        private String content;
        private String imageUrl;
        private String createdAt;
        private int rating;

        public ReviewDto(Review review) {
            this.id = review.getId();
            this.userName = review.getUser().getNickname() != null ? review.getUser().getNickname()
                    : review.getUser().getUsername(); // Fallback to username instead of email
            this.storeName = review.getStore() != null ? review.getStore().getStoreName() : "일반";
            this.content = review.getContent();
            this.imageUrl = review.getImageUrl();
            this.createdAt = review.getCreatedAt().toString();
            this.rating = review.getRating();
        }
    }
}
