package com.Gym_PT.Gym_PT.controller;

import com.Gym_PT.Gym_PT.auth.GoogleTokenVerifier;
import com.Gym_PT.Gym_PT.auth.JwtService;
import com.Gym_PT.Gym_PT.entity.Users;
import com.Gym_PT.Gym_PT.service.UsersService;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

        // ✅ 클라이언트에서 받은 원본 idToken 로그
        System.out.println("📩 received idToken from client: " + idToken);

        if (idToken == null || idToken.isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "idToken required"));

        try {
            // ✅ 토큰 검증 (aud/iss/exp 포함되어야 안전)
            JWTClaimsSet c = verifier.verify(idToken);
            System.out.println("✅ verified Google token claims: " + c.toJSONObject());

            String sub = c.getSubject();
            String email = (String) c.getClaim("email");
            String name = (String) c.getClaim("name");
            String picture = (String) c.getClaim("picture");
            Boolean emailVerified = (Boolean) c.getClaim("email_verified");

            // ✅ upsert 로그
            System.out.println("👤 upserting user (sub=" + sub + ", email=" + email + ")");

            Users u = usersService.upsertFromGoogle(sub, email, name, picture, emailVerified);

            String access = jwtService.createAccess(u.getId().toString());
            String refresh = jwtService.createRefresh(u.getId().toString()); // 추후 회전/쿠키 저장 확장 가능

            // ✅ 발급 토큰 로그 (access는 길 수 있으니 앞부분만 출력)
            System.out.println("🎫 issued tokens: access=" +
                    (access != null ? access.substring(0, Math.min(20, access.length())) + "..." : "null")
                    + ", refresh=" +
                    (refresh != null ? refresh.substring(0, Math.min(20, refresh.length())) + "..." : "null"));

            return ResponseEntity.ok(Map.of(
                    "data", Map.of("access", access, "refresh", refresh, "expiresIn", 3600)
            ));
        } catch (IllegalArgumentException e) {
            System.out.println("⛔ invalid idToken: " + e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid idToken"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Server error"));
        }
    }
}

