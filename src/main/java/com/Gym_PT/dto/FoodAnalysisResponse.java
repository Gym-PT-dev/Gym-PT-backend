package com.Gym_PT.dto;

public record FoodAnalysisResponse(
        String name,
        String amount,
        Integer kcal,
        Integer carbs,
        Integer protein,
        Integer fat
) {}