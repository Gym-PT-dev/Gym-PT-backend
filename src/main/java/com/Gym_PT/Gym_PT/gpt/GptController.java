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
        // Users가 없으면 UsersService#getById에서 예외 발생
        Users user = usersService.getById(userId);

        // 프로필 엔티티 조회 (없으면 내부에서 예외)
        UserProfile profile = userProfileService.getEntityByUserIdOrThrow(userId);

        String result = gptRoutineService.generateRoutine(user, profile);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/diet/{userId}")
    public ResponseEntity<String> getDiet(@PathVariable Long userId) {
        // 존재 확인 (없으면 예외)
        usersService.getById(userId);

        // 프로필 엔티티 조회 (없으면 내부에서 예외)
        UserProfile profile = userProfileService.getEntityByUserIdOrThrow(userId);

        String result = gptDietService.generateDiet(profile);
        return ResponseEntity.ok(result);
    }
}
