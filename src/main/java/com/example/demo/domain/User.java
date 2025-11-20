package com.example.demo.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "baro_users") // 👈 이름 변경!
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "user_name")
    private String username;

    private String password;
    private String nickname;
    private String email;
    private String role;
    private String provider;
    private String phoneNumber;
    private String profileImageUrl;
    private String preferredFood;
}