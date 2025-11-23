package com.Gym_PT.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FoodItem(
        String name,
        @JsonProperty("food_id") String foodId,
        String description
        // 필요에 따라 여기에 영양 정보 필드를 추가하세요.
        // 예: @JsonProperty("calories") String calories
) {}