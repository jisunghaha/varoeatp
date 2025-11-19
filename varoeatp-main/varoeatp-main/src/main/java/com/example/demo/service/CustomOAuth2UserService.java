package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // --- 1. 'kakao_account' 대신 'properties'에서 닉네임 추출 ---
        // (참고: Kakao API 응답 구조가 변경되어 'properties' 또는 'kakao_account' 내 'profile'을 사용합니다)
        Map<String, Object> properties;
        String nickname;

        if (attributes.containsKey("properties")) {
            properties = (Map<String, Object>) attributes.get("properties");
            nickname = (String) properties.get("nickname");
        } else {
            // 'properties'가 없는 구버전 응답 대비
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            nickname = (String) profile.get("nickname");
        }

        // --- 2. email 관련 로직을 모두 제거하고, Kakao 고유 ID를 username으로 사용 ---
        // String email = (String) kakaoAccount.get("email"); // 👈 삭제

        String kakaoId = attributes.get("id").toString();
        String username = kakaoId + "_kakao"; // 👈 (예: "12345678_kakao")

        // --- 3. (수정) email.split() 대신 kakaoId를 사용 ---
        // String username = email.split("@")[0] + "_kakao"; // 👈 삭제

        Optional<User> userOptional = userRepository.findByUsername(username);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            user = User.builder()
                    .username(username)
                    .nickname(nickname)
                    // .email(email) // 👈 4. email 필드 제거
                    .role("USER")
                    .provider("kakao")
                    .build();
            userRepository.save(user);
        }

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole())),
                attributes,
                userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName());
    }
}