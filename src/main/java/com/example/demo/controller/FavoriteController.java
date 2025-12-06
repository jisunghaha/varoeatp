package com.example.demo.controller;

import com.example.demo.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

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
            // Fallback for other providers or if id is missing
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

    // Toggle Favorite (Add/Remove)
    @PostMapping("/{storeId}")
    public ResponseEntity<String> toggleFavorite(@PathVariable Long storeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = getUserIdentifier(authentication);

        if (username == null || username.equals("anonymousUser")) {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }

        try {
            String result = favoriteService.toggleFavorite(storeId, username);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Get My Favorites (List of Store IDs)
    @GetMapping
    public ResponseEntity<List<Long>> getMyFavorites() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = getUserIdentifier(authentication);

        if (username == null || username.equals("anonymousUser")) {
            return ResponseEntity.ok(List.of());
        }

        try {
            List<Long> favorites = favoriteService.getMyFavorites(username);
            return ResponseEntity.ok(favorites);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}
