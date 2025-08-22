package com.Gym_PT.Gym_PT.gpt;

import com.Gym_PT.Gym_PT.entity.UserProfile;
import com.Gym_PT.Gym_PT.entity.Users;
import com.Gym_PT.Gym_PT.service.UserProfileService;
import com.Gym_PT.Gym_PT.service.UsersService;
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

    @GetMapping("/routine/{userId}")
    public ResponseEntity<String> getRoutine(@PathVariable("userId") Long userId) {
        Users user = usersService.getUser(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        UserProfile profile = userProfileService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found for userId: " + userId));

        String result = gptRoutineService.generateRoutine(user, profile);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/diet/{userId}")
    public ResponseEntity<String> getDiet(@PathVariable Long userId) {
        // Users는 필요 없지만 일관성을 위해 존재 확인
        usersService.getUser(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        UserProfile profile = userProfileService.getByUserId(userId)
                .orElseThrow(() -> new RuntimeException("UserProfile not found for userId: " + userId));

        String result = gptDietService.generateDiet(profile);
        return ResponseEntity.ok(result);
    }
}
