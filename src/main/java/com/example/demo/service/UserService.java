package com.example.demo.service;

import com.example.demo.auth.RegisterRequest;
import com.example.demo.domain.Reservation;
import com.example.demo.domain.User;
import com.example.demo.domain.FoodType;
import com.example.demo.dto.UserProfileUpdateRequest;
import com.example.demo.dto.UserProfileResponse;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    // 사용자 찾기 헬퍼
    private User findUserByAnyMeans(String identifier) {
        Optional<User> userOpt = userRepository.findByUsername(identifier);
        if (userOpt.isPresent()) return userOpt.get();

        userOpt = userRepository.findByEmail(identifier);
        if (userOpt.isPresent()) return userOpt.get();

        userOpt = userRepository.findByUsername(identifier + "_kakao");
        if (userOpt.isPresent()) return userOpt.get();

        throw new RuntimeException("사용자를 찾을 수 없습니다: " + identifier);
    }

    @Transactional
    public User registerUser(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 사용자 이름입니다.");
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 가입된 이메일입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role("USER")
                .build();

        return userRepository.save(user);
    }

    // 👇 [수정됨] 회원 탈퇴 로직 (심플 버전)
    @Transactional
    public void deleteUser(String identifier, String password) {
        User user = findUserByAnyMeans(identifier);

        // 일반 유저인 경우에만 비밀번호 확인 (소셜 유저는 패스)
        if (user.getProvider() == null || user.getProvider().isEmpty()) {
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("비밀번호가 일치하지 않습니다.");
            }
        }

        // 1. 유저의 예약 내역을 모두 가져옵니다.
        List<Reservation> userReservations = reservationRepository.findByUserOrderByIdDesc(user);

        // 2. 예약 내역을 삭제합니다.
        // * Reservation.java에 CascadeType.ALL 설정을 했으므로,
        // * 예약을 지우면 안에 든 메뉴(Item)들도 자동으로 삭제됩니다. (복잡한 로직 불필요)
        if (!userReservations.isEmpty()) {
            reservationRepository.deleteAll(userReservations);
        }

        // 3. 깨끗해진 유저를 삭제합니다.
        userRepository.delete(user);
    }

    @Transactional
    public void updateUserProfile(String identifier, UserProfileUpdateRequest request) {
        User user = findUserByAnyMeans(identifier);

        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            user.setNickname(request.getNickname());
        }

        if (request.getPreferredFoods() != null) {
            String foodString = String.join(",", request.getPreferredFoods());
            user.setPreferredFood(foodString);
        }
    }

    public UserProfileResponse getUserProfile(String identifier) {
        User user = findUserByAnyMeans(identifier);

        List<FoodType> foodList = new ArrayList<>();
        if (user.getPreferredFood() != null && !user.getPreferredFood().isEmpty()) {
            try {
                foodList = Arrays.stream(user.getPreferredFood().split(","))
                        .map(String::trim)
                        .map(FoodType::valueOf)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // 무시
            }
        }

        return UserProfileResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .preferredFoods(foodList)
                .build();
    }
}