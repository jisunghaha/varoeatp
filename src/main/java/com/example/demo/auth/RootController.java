// RootController.java
package com.example.demo.auth;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping("/")
    public String root() {
        // resources/templates 폴더의 메인 HTML 파일 이름
        return "baroeat_interface"; 
    }
}