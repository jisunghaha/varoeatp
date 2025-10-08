package com.example.demo;

import com.example.demo.config.SecurityConfig; // SecurityConfig import
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import; // Import 어노테이션

@SpringBootTest
@Import(SecurityConfig.class) // <-- 이 한 줄을 추가!
class BaroeatpApplicationTests {

    @Test
    void contextLoads() {
    }

}