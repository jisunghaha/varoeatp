package com.example.demo.auth;

public class RegisterRequest {
    private String userName; // 이름
    private String email;    // 이메일 (아이디)
    private String phoneNumber; // 휴대폰 번호
    private String password; // 비밀번호

    // --- Getter & Setter ---
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
