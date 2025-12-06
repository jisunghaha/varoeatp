package com.example.demo.service;

import com.example.demo.domain.Favorite;
import com.example.demo.domain.Store;
import com.example.demo.domain.User;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.StoreRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional
    public String toggleFavorite(Long storeId, String username) {
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("매장을 찾을 수 없습니다."));

        Optional<Favorite> existingFavorite = favoriteRepository.findByUserAndStore(user, store);

        if (existingFavorite.isPresent()) {
            favoriteRepository.delete(existingFavorite.get());
            return "removed";
        } else {
            Favorite favorite = new Favorite(user, store);
            favoriteRepository.save(favorite);
            return "added";
        }
    }

    public List<Long> getMyFavorites(String username) {
        User user = userRepository.findByEmail(username)
                .or(() -> userRepository.findByUsername(username))
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<Favorite> favorites = favoriteRepository.findByUser(user);
        return favorites.stream()
                .map(f -> f.getStore().getId())
                .collect(Collectors.toList());
    }
}
