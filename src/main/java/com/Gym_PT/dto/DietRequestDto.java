package com.Gym_PT.dto;

import com.Gym_PT.entity.DietMeal.MealType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DietRequestDto {
    private LocalDate date; // 식단 날짜
    private List<DietMealDto> meals;

    @Getter @Setter
    @NoArgsConstructor
    public static class DietMealDto {
        private MealType mealType; // BREAKFAST, LUNCH, DINNER, SNACK
        private List<FoodItemDto> foods;
    }

    @Getter @Setter
    @NoArgsConstructor
    public static class FoodItemDto {
        private String name;
        private String amount;
        private Integer kcal;
        private Integer carbs;
        private Integer protein;
        private Integer fat;
    }
}