package com.Gym_PT.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FoodResponseItem(
        @JsonProperty("food_id") Long foodId,
        @JsonProperty("food_entry_name") String foodName,
        @JsonProperty("eaten") EatenInfo eaten
) {
    // [추가] 안전하게 칼로리를 꺼내는 헬퍼 메소드
    public double getSafeCalories() {
        if (eaten != null
                && eaten.nutrition() != null
                && eaten.nutrition().calories() != null) {
            try {
                return Double.parseDouble(eaten.nutrition().calories());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0; // 영양 정보가 없으면 0 반환
    }
}