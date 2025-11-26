package com.Gym_PT.controller;

import com.Gym_PT.dto.AddFoodSaveRequest;
import com.Gym_PT.dto.DietResponseDto;
import com.Gym_PT.service.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diet/food")
@RequiredArgsConstructor
public class FoodItemController {

    private final DietService dietService;

    /**
     * GPT 분석 결과로 나온 음식 1개를
     * 특정 사용자 / 날짜 / 끼니에 추가하고,
     * 하루 전체 식단(DietResponseDto)을 반환한다.
     *
     * POST /api/diet/food
     */
    @PostMapping
    public ResponseEntity<DietResponseDto> addFood(@RequestBody AddFoodSaveRequest request) {
        DietResponseDto dto = dietService.addFood(request);
        return ResponseEntity.ok(dto);
    }
}
