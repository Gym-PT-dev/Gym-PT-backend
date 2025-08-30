// com.Gym_PT.Gym_PT.controller.AuthController.java
package com.Gym_PT.Gym_PT.controller;

import com.Gym_PT.Gym_PT.auth.GoogleTokenVerifier;
import com.Gym_PT.Gym_PT.auth.JwtService;
import com.Gym_PT.Gym_PT.entity.Users;
import com.Gym_PT.Gym_PT.service.UsersService;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final GoogleTokenVerifier verifier;
    private final UsersService usersService;
    private final JwtService jwtService;

    @PostMapping("/google")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String idToken = body.get("idToken");
        if (idToken == null || idToken.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "idToken required"));
        }

        try {
            // 1) 토큰 검증 및 클레임 추출
            JWTClaimsSet c = verifier.verify(idToken);
            String sub           = c.getSubject();
            String email         = (String) c.getClaim("email");
            String name          = (String) c.getClaim("name");
            String picture       = (String) c.getClaim("picture");
            Boolean emailVerified = (Boolean) c.getClaim("email_verified");

            // 2) 사용자 upsert
            Users u = usersService.upsertFromGoogle(sub, email, name, picture, emailVerified);

            // 3) 토큰 발급
            String access  = jwtService.createAccess(u.getId().toString());
            String refresh = jwtService.createRefresh(u.getId().toString());

            // 4) 프론트로 보낼 user 객체(엔티티 전체 중 노출 가능한 필드)
            Map<String, Object> userDto = new LinkedHashMap<>();
            userDto.put("id",            u.getId());
            userDto.put("googleSub",     u.getGoogleSub());
            userDto.put("email",         u.getEmail());
            userDto.put("name",          u.getName());
            userDto.put("picture",       u.getPicture());
            userDto.put("emailVerified", u.getEmailVerified());
            userDto.put("createdAt",     u.getCreatedAt());
            userDto.put("updatedAt",     u.getUpdatedAt());

            // 5) 통일된 응답 형태
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("accessToken",  access);
            resp.put("refreshToken", refresh);
            resp.put("user",         userDto);

            return ResponseEntity.ok(resp);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid idToken"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Server error"));
        }
    }
}
