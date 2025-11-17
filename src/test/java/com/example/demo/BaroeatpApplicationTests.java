package com.example.demo;

import com.example.demo.config.SecurityConfig;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
@Disabled
@SpringBootTest
@Import(SecurityConfig.class)
class BaroeatpApplicationTests {

    @Test
    void contextLoads() {
    }

}