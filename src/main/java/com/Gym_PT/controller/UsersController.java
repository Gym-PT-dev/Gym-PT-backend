package com.Gym_PT.controller;

import com.Gym_PT.entity.Users;
import com.Gym_PT.service.UsersService;
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

    @PatchMapping("/me")
    public ResponseEntity<?> updateMe(
            Authentication auth,
            @RequestBody Map<String, String> body
    ) {
        Long userId = Long.valueOf(auth.getName());
        String newName = body.get("name");   // 프론트에서 { "name": "새이름" } 보내도록

        Users u = usersService.updateName(userId, newName);

        // 응답 형식은 GET /me랑 맞춰주기
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "name", u.getName(),
                "picture", u.getPicture(),
                "emailVerified", u.getEmailVerified()
        ));
    }
}
