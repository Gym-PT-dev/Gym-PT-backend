package com.Gym_PT.dto;

public record FoodAnalysisRequest(
        String foodName, // 예: "닭가슴살", "김치찌개"
        String amount    // 예: "100g", "1인분", "한 그릇"
) {}