package com.Gym_PT.service;

import com.Gym_PT.dto.DietRequestDto;
import com.Gym_PT.entity.DietMeal.MealType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FatSecretConversionService {

    private final ObjectMapper objectMapper;

    public DietRequestDto convertToDietDto(String fatSecretJson, MealType mealType) {
        try {
            JsonNode root = objectMapper.readTree(fatSecretJson);

            // 🔥 FatSecret 실제 루트: "food_response" (배열)
            JsonNode foodResponse = root.path("food_response");

            List<DietRequestDto.FoodDto> foodDtos = new ArrayList<>();

            if (foodResponse.isArray()) {
                for (JsonNode item : foodResponse) {

                    String foodName = item.path("food_entry_name").asText(null);
                    JsonNode eaten = item.path("eaten");
                    JsonNode totalNutri = eaten.path("total_nutritional_content");

                    if (foodName == null || totalNutri.isMissingNode()) {
                        continue;
                    }

                    // ----- 양/단위 정보 -----
                    double units = eaten.path("units").asDouble(0);
                    String singularDesc = eaten.path("singular_description").asText("");
                    String pluralDesc = eaten.path("plural_description").asText("");
                    String unitDesc = units <= 1.0 && !singularDesc.isEmpty()
                            ? singularDesc
                            : pluralDesc;

                    double totalMetricAmount = eaten.path("total_metric_amount").asDouble(0);
                    String metricDesc = eaten.path("metric_description").asText("g");

                    String amount = String.format(
                            Locale.US,
                            "%.1f %s (%.0f %s)",
                            units,
                            unitDesc,
                            totalMetricAmount,
                            metricDesc
                    ).trim();

                    // ----- 영양 성분 (총량 기준) -----
                    double calories = parseDoubleSafe(totalNutri.path("calories").asText("0"));
                    double carbs = parseDoubleSafe(totalNutri.path("carbohydrate").asText("0"));
                    double protein = parseDoubleSafe(totalNutri.path("protein").asText("0"));
                    double fat = parseDoubleSafe(totalNutri.path("fat").asText("0"));

                    DietRequestDto.FoodDto foodDto = new DietRequestDto.FoodDto();
                    foodDto.setName(foodName);
                    foodDto.setAmount(amount);
                    foodDto.setKcal((int) Math.round(calories));
                    foodDto.setCarbs(carbs);
                    foodDto.setProtein(protein);
                    foodDto.setFat(fat);

                    foodDtos.add(foodDto);
                }
            }

            // ----- Meal DTO 구성 -----
            DietRequestDto.MealDto mealDto = new DietRequestDto.MealDto();
            mealDto.setMealType(mealType);
            mealDto.setFoods(foodDtos);

            DietRequestDto dto = new DietRequestDto();
            dto.setMeals(Collections.singletonList(mealDto));

            System.out.println("[FatSecretConversionService] Parsed items = " + foodDtos.size());
            return dto;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse FatSecret JSON", e);
        }
    }

    private double parseDoubleSafe(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {
            return 0;
        }
    }
}
