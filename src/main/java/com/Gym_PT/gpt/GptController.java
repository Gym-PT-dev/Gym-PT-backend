package com.Gym_PT.gpt;

import com.Gym_PT.entity.*;
import com.Gym_PT.gpt.GptDietService;
import com.Gym_PT.gpt.GptRoutineService;
import com.Gym_PT.service.UserProfileService;
import com.Gym_PT.service.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gpt")
@RequiredArgsConstructor
public class GptController {

    private final GptRoutineService gptRoutineService;
    private final GptDietService gptDietService;
    private final UsersService usersService;
    private final UserProfileService userProfileService;

    // 1. 운동 루틴 생성 (GET /api/gpt/routine/{userId})
    @GetMapping("/routine/{userId}")
    public ResponseEntity<String> getRoutine(@PathVariable Long userId) {
        Users user = usersService.getById(userId);
        UserProfile profile = userProfileService.getEntityByUserIdOrThrow(userId);
        return ResponseEntity.ok(gptRoutineService.generateRoutine(user, profile));
    }

    // 2. 식단 생성 (GET /api/gpt/diet/{userId})
    @GetMapping("/diet/{userId}")
    public ResponseEntity<String> getDiet(@PathVariable Long userId) {
        usersService.getById(userId);
        UserProfile profile = userProfileService.getEntityByUserIdOrThrow(userId);
        // userId를 넘겨서 7일 기록 조회하도록 함
        return ResponseEntity.ok(gptDietService.generateDiet(userId, profile));
    }
}