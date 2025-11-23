package com.Gym_PT.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Nutrition(
        String calories,      // 칼로리
        String carbohydrate,  // 탄수화물
        String protein,       // 단백질
        String fat,           // 지방
        String sugar,         // 당류
        String sodium,        // 나트륨
        String cholesterol,   // 콜레스테롤
        String fiber,         // 식이섬유
        String potassium      // 칼륨
) {}