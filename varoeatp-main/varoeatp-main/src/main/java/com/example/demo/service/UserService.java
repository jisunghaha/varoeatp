package com.example.demo.service;

import com.example.demo.auth.RegisterRequest;
import com.example.demo.domain.FoodType; 
import com.example.demo.domain.Reservation; 
import com.example.demo.domain.User;
import com.example.demo.dto.UserProfileResponse; 
import com.example.demo.dto.UserProfileUpdateRequest; 
import com.example.demo.repository.ReservationRepository; 
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; 

import java.io.File; 
import java.io.IOException; 
import java.util.ArrayList; 
import java.util.List; 
import java.util.UUID; 
import java.util.stream.Collectors; 

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository; 
    private final PasswordEncoder passwordEncoder;

   private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/"   ;
    
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
                .role("USER")
                .build();

        return userRepository.save(user);
    }

    

    //프로필 조회
    public UserProfileResponse getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        List<FoodType> foodList = new ArrayList<>();
        
        // DB에 저장된 선호음식 문자열을 꺼내서 리스트로 변환
        if (user.getPreferredFood() != null && !user.getPreferredFood().isEmpty()) {
            String[] foods = user.getPreferredFood().split(",");
            for (String f : foods) {
                try {
                    foodList.add(FoodType.valueOf(f.trim()));
                } catch (IllegalArgumentException e) {
                    
                }
            }
        }

        return UserProfileResponse.builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .preferredFoods(foodList)
                .build();
    }

    /**
     * 2. 프로필 수정 (리스트 -> DB 문자열 변환 저장)
     */
    @Transactional // 👈 DB를 수정하므로 꼭 붙여야 합니다.
    public void updateProfileInfo(String email, UserProfileUpdateRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }

        // 리스트를 쉼표로 이어진 문자열로 변환 ("KOREAN,WESTERN")
        if (request.getPreferredFoods() != null) {
            if (request.getPreferredFoods().isEmpty()) {
                user.setPreferredFood("");
            } else {
                String joinedString = request.getPreferredFoods().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(","));
                user.setPreferredFood(joinedString);
            }
        }
        userRepository.save(user);
    }
    

    /**
     * 3. 프로필 사진 업로드
     */
    @Transactional // 👈 DB 수정
    public String uploadProfileImage(String email, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new RuntimeException("파일이 비어있습니다.");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 파일 이름 중복 방지
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        File saveFile = new File(UPLOAD_DIR + fileName);

        // 폴더 없으면 생성
        if (!saveFile.getParentFile().exists()) {
            saveFile.getParentFile().mkdirs();
        }

        // 실제 저장
        file.transferTo(saveFile);

        // DB에 경로 업데이트
        String dbFilePath = "/images/" + fileName;
        user.setProfileImageUrl(dbFilePath);
        
        return dbFilePath;
    }

 
    public List<Reservation> getReservationHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));
        return reservationRepository.findAllByUser(user);
    }

   

    @Transactional
    public void deleteUser(String username, String password) {
    
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

    
        userRepository.delete(user);
    }
}