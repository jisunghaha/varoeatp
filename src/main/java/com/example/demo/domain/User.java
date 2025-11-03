package com.example.demo.domain; // 이 패키지 이름은 본인 프로젝트에 맞게 유지하세요.

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users") // 1. DB 테이블 이름이 'users'라고 명시
public class User {

    @Id
    @Column(name = "user_id") // 2. 'id' 필드는 'user_id' 컬럼에 해당
    private Long id;

    @Column(name = "email") // email은 이름이 같지만, 통일성을 위해 추가
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "user_name") // 3. 'userName' 필드는 'user_name' 컬럼에 해당
    private String userName;

    @Column(name = "phone_number") // 4. 'phoneNumber' 필드는 'phone_number' 컬럼에 해당
    private String phoneNumber;

    @Column(name = "login_type") // 5. 'provider' 필드는 'login_type' 컬럼에 해당 (가장 중요!)
    private String provider;
    
    // Lombok을 사용하지 않는 경우, 아래 Getter/Setter가 필요합니다.
    // (Lombok의 @Getter, @Setter 어노테이션이 있다면 아래 코드는 없어도 됩니다.)
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}