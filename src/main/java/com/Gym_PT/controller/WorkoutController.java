package com.Gym_PT.controller;

import com.Gym_PT.dto.WorkoutRequestDto;
import com.Gym_PT.dto.WorkoutResponseDto;
import com.Gym_PT.service.WorkoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    /**
     * 특정 사용자(userId)의 해당 날짜(WorkoutRequestDto.date)에 대한
     * 운동 기록을 저장하거나 갱신하는 API
     *
     * 예: POST /users/1/workout
     */
    @PostMapping("/{userId}/workout")
    public ResponseEntity<WorkoutRequestDto> saveWorkout(
            @PathVariable Long userId,
            @RequestBody WorkoutRequestDto request
    ) {
        WorkoutRequestDto savedWorkout = workoutService.saveWorkout(userId, request);
        return ResponseEntity.ok(savedWorkout);
    }


    @GetMapping("/{userId}/workout")
    public ResponseEntity<WorkoutResponseDto> getWorkoutByDate(
            @PathVariable Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        WorkoutResponseDto result = workoutService.getWorkoutByUserAndDate(userId, date);

        if (result == null) {
            // 해당 날짜에 운동 기록이 없는 경우
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }


    @DeleteMapping("/{userId}/workout")
    public ResponseEntity<Void> deleteWorkoutByDate(
            @PathVariable Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        workoutService.deleteWorkoutByUserAndDate(userId, date);
        return ResponseEntity.noContent().build(); // 204 응답
    }
}
