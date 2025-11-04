// src/main/java/com/Gym_PT/controller/UserProfileController.java
package com.Gym_PT.controller;

import com.Gym_PT.dto.UserProfileDto;
import com.Gym_PT.dto.UserProfileRequest;
import com.Gym_PT.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserProfileController {

    private final UserProfileService profileService;

    /** 현재 로그인한 사용자 프로필 조회 */
    @GetMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> getMyProfile(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = Long.valueOf(auth.getName());
        return profileService.getByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** 현재 로그인한 사용자 프로필 업서트(전체 저장/교체) */
    @PutMapping("/me/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDto> upsertMyProfile(
            Authentication auth,
            @Valid @RequestBody UserProfileRequest body
    ) {
        if (auth == null || auth.getName() == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = Long.valueOf(auth.getName());
        UserProfileDto saved = profileService.upsert(userId, body);
        return ResponseEntity.ok(saved);
    }
}
