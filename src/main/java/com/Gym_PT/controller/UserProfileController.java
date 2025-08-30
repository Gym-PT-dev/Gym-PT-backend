package com.Gym_PT.controller;

import com.Gym_PT.dto.UserProfileDto;
import com.Gym_PT.dto.UserProfileRequest;
import com.Gym_PT.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/{userId}/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService profileService;

    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable Long userId) {
        return profileService.getByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<UserProfileDto> upsertProfile(
            @PathVariable Long userId,
            @RequestBody UserProfileRequest body
    ) {
        return ResponseEntity.ok(profileService.upsert(userId, body));
    }
}
