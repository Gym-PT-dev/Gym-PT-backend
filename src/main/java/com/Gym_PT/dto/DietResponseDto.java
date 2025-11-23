package com.Gym_PT.dto;

import com.Gym_PT.entity.DietMeal.MealType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DietResponseDto {
    private Long id;        // Record ID
    private LocalDate date;
    private Integer totalDailyKcal;
    private List<DietMealResponseDto> meals;

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DietMealResponseDto {
        private MealType mealType;
        private Integer totalKcal;
        private List<FoodItemResponseDto> foods;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FoodItemResponseDto {
        private String name;
        private String amount;
        private Integer kcal;
        private Integer carbs;
        private Integer protein;
        private Integer fat;
    }
}