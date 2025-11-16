package com.Gym_PT.controller;

import com.Gym_PT.dto.WorkoutRequestDto;
import com.Gym_PT.dto.WorkoutResponseDto;
import com.Gym_PT.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @PostMapping("/{userId}/workout")
    public ResponseEntity<WorkoutRequestDto> saveWorkout(
            @PathVariable Long userId,
            @RequestBody WorkoutRequestDto request
    ) {
        WorkoutRequestDto savedWorkout = workoutService.saveWorkout(userId, request);
        return ResponseEntity.ok(savedWorkout);
    }

    // 🔹 하루에 여러 WorkoutRecord가 있을 수 있으므로 List<WorkoutResponseDto> 반환
    @GetMapping("/{userId}/workout")
    public ResponseEntity<List<WorkoutResponseDto>> getWorkoutByDate(
            @PathVariable Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<WorkoutResponseDto> result =
                workoutService.getWorkoutListByUserAndDate(userId, date);

        // 비어 있으면 [] 그대로 200으로 내려보내도 되고,
        // 필요하면 notFound() 로 바꿔도 됨
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{userId}/workout")
    public ResponseEntity<Void> deleteWorkoutByDate(
            @PathVariable Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        workoutService.deleteWorkoutByUserAndDate(userId, date);
        return ResponseEntity.noContent().build();
    }
}
