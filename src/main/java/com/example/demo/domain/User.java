package com.example.demo.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(name = "email") // 아이디 역할
    private String email; 

    @Column(name = "password")
    private String password;

    @Column(name = "user_name") // DB 컬럼명 user_name 매핑
    private String userName; // 자바 변수명은 userName (camelCase)

    @Column(name = "phone_number") // 휴대폰 번호 컬럼 매핑
    private String phoneNumber;
    private String provider;
    public User(String userName, String email, String provider) {
        this.userName = userName;
        this.email = email;
        this.provider = provider;
    }
    
    // Getters and setters (추가된 필드 포함)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getUserName() { return userName; } // user_name의 Getter/Setter
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getPhoneNumber() { return phoneNumber; } // phone_number의 Getter/Setter
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
}

