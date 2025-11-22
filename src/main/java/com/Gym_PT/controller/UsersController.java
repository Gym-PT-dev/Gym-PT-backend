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

    // 🔹 인증 정보에서 userId(Long) 안전하게 꺼내는 헬퍼
    private Long extractUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() ||
                "anonymousUser".equals(auth.getName())) {
            throw new IllegalStateException("UNAUTHORIZED");
        }

        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("INVALID_PRINCIPAL");
        }
    }

    // 🔹 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        try {
            Long userId = extractUserId(auth);  // ✅ 인증 + 숫자 변환 체크
            Users u = usersService.getById(userId);

            return ResponseEntity.ok(Map.of(
                    "id",            u.getId(),
                    "email",         u.getEmail(),
                    "name",          u.getName(),         // 구글 원본 이름
                    "displayName",   u.getDisplayName(),  // 앱에서 쓰는 표시 이름
                    "picture",       u.getPicture(),
                    "emailVerified", u.getEmailVerified()
            ));
        } catch (IllegalStateException e) {
            String code = e.getMessage();
            if ("UNAUTHORIZED".equals(code) || "INVALID_PRINCIPAL".equals(code)) {
                return ResponseEntity
                        .status(401)
                        .body(Map.of("error", "Unauthorized"));
            }
            // 혹시 다른 코드가 들어왔을 경우 방어
            return ResponseEntity
                    .status(500)
                    .body(Map.of("error", "Server error"));
        }
    }

    // 🔹 내 정보 수정 (표시 이름만 변경)
    @PatchMapping("/me")
    public ResponseEntity<?> updateMe(
            Authentication auth,
            @RequestBody Map<String, String> body
    ) {
        try {
            Long userId = extractUserId(auth);  // ✅ 인증 + 숫자 변환 체크

            // 프론트에서 { "displayName": "새 닉네임" } 로 보낸다고 가정
            String newDisplayName = body.get("displayName");

            if (newDisplayName == null || newDisplayName.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "displayName is required"));
            }

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

        } catch (IllegalStateException e) {
            String code = e.getMessage();
            if ("UNAUTHORIZED".equals(code) || "INVALID_PRINCIPAL".equals(code)) {
                return ResponseEntity
                        .status(401)
                        .body(Map.of("error", "Unauthorized"));
            }
            return ResponseEntity
                    .status(500)
                    .body(Map.of("error", "Server error"));
        }
    }
}
