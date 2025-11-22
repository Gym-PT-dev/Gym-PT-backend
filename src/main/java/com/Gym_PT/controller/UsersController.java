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

    // 🔹 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        Long userId = Long.valueOf(auth.getName()); // JWT subject에 Users.id 저장돼 있다고 가정
        Users u = usersService.getById(userId);

        return ResponseEntity.ok(Map.of(
                "id",            u.getId(),
                "email",         u.getEmail(),
                "name",          u.getName(),         // 구글 원본 이름
                "displayName",   u.getDisplayName(),  // 앱에서 쓰는 표시 이름
                "picture",       u.getPicture(),
                "emailVerified", u.getEmailVerified()
        ));
    }

    // 🔹 내 정보 수정 (표시 이름만 변경)
    @PatchMapping("/me")
    public ResponseEntity<?> updateMe(
            Authentication auth,
            @RequestBody Map<String, String> body
    ) {
        Long userId = Long.valueOf(auth.getName());

        // 프론트에서 { "displayName": "새 닉네임" } 로 보낸다고 가정
        String newDisplayName = body.get("displayName");

        Users u = usersService.updateDisplayName(userId, newDisplayName);

        // 응답 형식은 GET /me와 최대한 맞춰 주기
        return ResponseEntity.ok(Map.of(
                "id",            u.getId(),
                "email",         u.getEmail(),
                "name",          u.getName(),
                "displayName",   u.getDisplayName(),
                "picture",       u.getPicture(),
                "emailVerified", u.getEmailVerified()
        ));
    }
}
