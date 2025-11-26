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

    /**
     * 🔹 운동 기록 저장
     *  - 요청: 날짜(LocalDate) + 운동 내용
     *  - DB: 서비스에서 LocalDate + 현재 시간 → LocalDateTime으로 저장
     *  - 응답: 지금은 요청 DTO 그대로 돌려주지만,
     *         필요하면 WorkoutResponseDto로 바꿔도 됨
     */
    @PostMapping("/{userId}/workout")
    public ResponseEntity<WorkoutRequestDto> saveWorkout(
            @PathVariable Long userId,
            @RequestBody WorkoutRequestDto request
    ) {
        WorkoutRequestDto savedWorkout = workoutService.saveWorkout(userId, request);
        return ResponseEntity.ok(savedWorkout);
    }

    /**
     * 🔹 하루에 여러 WorkoutRecord가 있을 수 있으므로 List<WorkoutResponseDto> 반환
     *  - 쿼리 파라미터 date는 여전히 LocalDate (yyyy-MM-dd)
     *  - 서비스 내부에서 해당 날짜의 00:00 ~ 다음날 00:00 사이 LocalDateTime 범위 조회
     *  - 응답 DTO의 date는 LocalDateTime (JSON 문자열)로 내려감
     */
    @GetMapping("/{userId}/workout")
    public ResponseEntity<List<WorkoutResponseDto>> getWorkoutByDate(
            @PathVariable Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<WorkoutResponseDto> result =
                workoutService.getWorkoutListByUserAndDate(userId, date);

        return ResponseEntity.ok(result);
    }

    /**
     * 🔹 해당 날짜의 운동 기록 전체 삭제
     *  - 조회와 마찬가지로 LocalDate로 받고
     *    서비스에서 LocalDateTime 범위로 변환해 삭제
     */
    @DeleteMapping("/{userId}/workout")
    public ResponseEntity<Void> deleteWorkoutByDate(
            @PathVariable Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        workoutService.deleteWorkoutByUserAndDate(userId, date);
        return ResponseEntity.noContent().build();
    }
}
