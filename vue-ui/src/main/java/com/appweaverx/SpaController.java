package com.appweaverx;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping("/")
    public String index() {
        return "index"; // Thymeleaf resolves templates/index.html
    }

}
