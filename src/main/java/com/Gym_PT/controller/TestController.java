package com.Gym_PT.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/")
    public String mainPage() {
        return "Test3";
    }
}
