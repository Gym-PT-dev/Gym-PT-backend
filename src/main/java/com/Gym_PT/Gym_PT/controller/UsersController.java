package com.Gym_PT.Gym_PT.controller;

import com.Gym_PT.Gym_PT.entity.Users;
import com.Gym_PT.Gym_PT.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UsersController {

    private final UsersService usersService;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        Long userId = Long.valueOf(auth.getName()); // JWT subject에 Users.id 저장
        Users u = usersService.getById(userId);
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "name", u.getName(),
                "picture", u.getPicture(),
                "emailVerified", u.getEmailVerified()
        ));
    }
}