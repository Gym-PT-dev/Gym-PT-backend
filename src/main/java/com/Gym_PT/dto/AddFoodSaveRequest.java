package com.Gym_PT.dto;

import com.Gym_PT.entity.DietMeal.MealType;

import java.time.LocalDate;

/**
 * GPT/프론트에서 음식 1개 저장할 때 사용하는 요청 DTO
 */
public record AddFoodSaveRequest(
        Long userId,        // 사용자 ID
        LocalDate date,     // 식단 날짜 (YYYY-MM-DD)
        MealType mealType,  // BREAKFAST / LUNCH / DINNER / SNACK
        String name,        // 음식 이름
        String amount,      // 예: "150g", "1인분"
        Integer kcal,
        Integer carbs,
        Integer protein,
        Integer fat
) {
}
