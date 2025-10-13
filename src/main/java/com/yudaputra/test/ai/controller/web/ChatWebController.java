package com.yudaputra.test.ai.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatWebController {

    @GetMapping("/")
    public String home() {
        return "chat";
    }
}