package com.example.demo;
import org.junit.jupiter.api.Disabled;
import com.example.demo.config.SecurityConfig; // SecurityConfig import
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import; // Import 어노테이션
@Disabled
@SpringBootTest
@Import(SecurityConfig.class)
class BaroeatpApplicationTests {

    @Test
    void contextLoads() {
    }

}