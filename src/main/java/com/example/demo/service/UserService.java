package com.example.demo.service;

import com.example.demo.auth.RegisterRequest;
import com.example.demo.domain.User;
import com.example.demo.repository.FavoriteRepository;
import com.example.demo.repository.ReservationMenuRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.ReviewRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // C(R)UD
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRepository favoriteRepository;
    private final ReservationMenuRepository reservationMenuRepository;

    /**
     * 회원가입 로직
     */
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

    @Transactional //
    public void deleteUser(String email, String password) {

        // 2. 이메일로 User 찾기 (로그인 시 이메일 사용하므로)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다.")); //

        // 3. 비밀번호 확인
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        deleteRelatedEntities(user);

        // 4. 비밀번호가 일치하면 사용자 삭제
        userRepository.delete(user);
    }

    @Transactional
    public void deleteUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        deleteRelatedEntities(user);

        userRepository.delete(user);
    }

    private void deleteRelatedEntities(User user) {
        // 1. Delete Reviews FIRST (because Review references Reservation)
        reviewRepository.deleteByUser(user);

        // 2. Delete Favorites
        favoriteRepository.deleteByUser(user);

        // 3. Delete Reservation Menus (Cascade doesn't work with JPQL delete)
        reservationMenuRepository.deleteByReservationUser(user);

        // 4. Delete Reservations
        reservationRepository.deleteByUser(user);
    }

    @Transactional
    public void updateProfile(String email, String nickname, String preferredFood) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        if (preferredFood != null && !preferredFood.isEmpty()) {
            user.setPreferredFood(preferredFood);
        }
    }

    @Transactional
    public void updateProfileByUsername(String username, String nickname, String preferredFood) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        if (preferredFood != null && !preferredFood.isEmpty()) {
            user.setPreferredFood(preferredFood);
        }
    }

    // 기존 getUserDetails는 username으로 찾았지만, 이제 email로 찾는 것이 안전함
    // 하지만 하위 호환성을 위해 남겨두거나, email로 찾도록 변경
    public User getUserDetails(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }
}