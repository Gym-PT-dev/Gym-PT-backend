package com.Gym_PT.controller;

import com.Gym_PT.dto.DietRequestDto;
import com.Gym_PT.dto.DietResponseDto;
import com.Gym_PT.service.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class DietController {

    private final DietService dietService;

    /**
     * 식단 저장 (GPT가 생성한 JSON을 프론트에서 여기로 쏴주면 저장)
     */
    @PostMapping("/{userId}/diet")
    public ResponseEntity<DietResponseDto> saveDiet(
            @PathVariable Long userId,
            @RequestBody DietRequestDto request) {

        // 저장 후 저장된 데이터를 ResponseDto로 반환
        DietResponseDto savedDiet = dietService.saveDiet(userId, request);
        return ResponseEntity.ok(savedDiet);
    }

    /**
     * 날짜별 식단 조회
     */
    @GetMapping("/{userId}/diet")
    public ResponseEntity<DietResponseDto> getDietByDate(
            @PathVariable Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        DietResponseDto result = dietService.getDietByUserAndDate(userId, date);
        return ResponseEntity.ok(result);
    }

    /**
     * 날짜별 식단 삭제
     */
    @DeleteMapping("/{userId}/diet")
    public ResponseEntity<Void> deleteDietByDate(
            @PathVariable Long userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        dietService.deleteDietByUserAndDate(userId, date);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}