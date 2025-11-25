package com.Gym_PT.service;

import com.Gym_PT.dto.DietRequestDto;
import com.Gym_PT.entity.DietMeal.MealType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FatSecretConversionService {

    private final ObjectMapper objectMapper;

    public DietRequestDto convertToDietDto(String fatSecretJson, MealType mealType) {
        try {
            JsonNode root = objectMapper.readTree(fatSecretJson);
            JsonNode foodsNode = root.path("items");
            List<DietRequestDto.FoodItemDto> foodList = new ArrayList<>();

            if (foodsNode.isArray()) {
                for (JsonNode item : foodsNode) {
                    String name = item.path("food_entry_name").asText("Unknown");
                    JsonNode nutri = item.path("eaten").path("total_nutritional_content");

                    int kcal = 0, carbs = 0, protein = 0, fat = 0;
                    if (!nutri.isMissingNode() && !nutri.isNull()) {
                        kcal = parseInt(nutri.path("calories").asText());
                        carbs = parseInt(nutri.path("carbohydrate").asText());
                        protein = parseInt(nutri.path("protein").asText());
                        fat = parseInt(nutri.path("fat").asText());
                    }

                    DietRequestDto.FoodItemDto dto = new DietRequestDto.FoodItemDto();
                    dto.setName(name);
                    dto.setAmount("1인분"); // 기본값
                    dto.setKcal(kcal);
                    dto.setCarbs(carbs);
                    dto.setProtein(protein);
                    dto.setFat(fat);
                    foodList.add(dto);
                }
            }

            DietRequestDto requestDto = new DietRequestDto();
            DietRequestDto.DietMealDto mealDto = new DietRequestDto.DietMealDto();
            mealDto.setMealType(mealType);
            mealDto.setFoods(foodList);
            requestDto.setMeals(List.of(mealDto));

            return requestDto; // 날짜(Date) 없이 반환 (프론트에서 설정)
        } catch (Exception e) {
            throw new RuntimeException("FatSecret Parsing Error", e);
        }
    }

    private int parseInt(String v) {
        try { return (int) Math.round(Double.parseDouble(v)); } catch (Exception e) { return 0; }
    }
}