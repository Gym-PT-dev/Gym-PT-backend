// com/Gym_PT/controller/DevAuthController.java
package com.Gym_PT.controller;

import com.Gym_PT.auth.JwtService;
import com.Gym_PT.entity.Users;
import com.Gym_PT.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/dev")
@RequiredArgsConstructor
public class DevAuthController {

    private final UsersRepository usersRepository;
    private final JwtService jwtService;

    // 👉 dev 환경 전용 로그인
    @PostMapping("/login")
    public ResponseEntity<?> devLogin(@RequestBody Map<String, String> body) {
        String email = body.getOrDefault("email", "admin@gympt.local");

        Users u = usersRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No such dev user: " + email));

        String access  = jwtService.createAccess(u.getId().toString());
        String refresh = jwtService.createRefresh(u.getId().toString());

        Map<String, Object> userDto = new LinkedHashMap<>();
        userDto.put("id",            u.getId());
        userDto.put("googleSub",     u.getGoogleSub());
        userDto.put("email",         u.getEmail());
        userDto.put("name",          u.getName());
        userDto.put("picture",       u.getPicture());
        userDto.put("emailVerified", u.getEmailVerified());
        userDto.put("createdAt",     u.getCreatedAt());
        userDto.put("updatedAt",     u.getUpdatedAt());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("accessToken",  access);
        resp.put("refreshToken", refresh);
        resp.put("user",         userDto);

        return ResponseEntity.ok(resp);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }
}
