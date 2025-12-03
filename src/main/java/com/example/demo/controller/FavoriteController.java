package com.example.demo.controller;

import com.example.demo.domain.Favorite;
import com.example.demo.domain.Store;
import com.example.demo.domain.User;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.StoreRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private UserRepository userRepository;

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
            return ((UserDetails) principal).getUsername(); // Assuming username is email
        } else if (principal instanceof String) {
            return (String) principal;
        }
        return null;
    }

    // Toggle Favorite (Add/Remove)
    @PostMapping("/{storeId}")
    public ResponseEntity<String> toggleFavorite(@PathVariable Long storeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromAuthentication(authentication);

        if (email == null || email.equals("anonymousUser")) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("매장을 찾을 수 없습니다."));

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndStore(user, store);

        if (existingFavorite.isPresent()) {
            favoriteRepository.delete(existingFavorite.get());
            return ResponseEntity.ok("removed");
        } else {
            Favorite favorite = new Favorite(user, store);
            favoriteRepository.save(favorite);
            return ResponseEntity.ok("added");
        }
    }

    // Get My Favorites (List of Store IDs)
    @GetMapping
    public ResponseEntity<List<Long>> getMyFavorites() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromAuthentication(authentication);

        if (email == null || email.equals("anonymousUser")) {
            return ResponseEntity.ok(List.of());
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Favorite> favorites = favoriteRepository.findByUser(user);
        List<Long> storeIds = favorites.stream()
                .map(f -> f.getStore().getId())
                .collect(Collectors.toList());

        return ResponseEntity.ok(storeIds);
    }
}
