package com.Gym_PT.service;

import com.Gym_PT.dto.DietRequestDto;
import com.Gym_PT.dto.DietResponseDto;
import com.Gym_PT.entity.*;
import com.Gym_PT.repository.DietRecordRepository;
import com.Gym_PT.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietService {

    private final DietRecordRepository dietRecordRepository;
    private final UsersRepository usersRepository;

    // 1. 식단 저장 (DTO -> Entity 변환)
    @Transactional
    public DietResponseDto saveDiet(Long userId, DietRequestDto request) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        // 해당 날짜에 이미 식단이 있으면 덮어쓰거나 에러 처리 (여기서는 삭제 후 재저장 방식 채택)
        dietRecordRepository.findByUser_IdAndDietDate(userId, request.getDate())
                .ifPresent(dietRecordRepository::delete);

        // DietRecord 생성
        DietRecord dietRecord = DietRecord.builder()
                .user(user)
                .dietDate(request.getDate())
                .totalDailyKcal(0)
                .build();

        int dailyTotalKcal = 0;

        // Meals & FoodItems 변환
        if (request.getMeals() != null) {
            for (DietRequestDto.DietMealDto mealDto : request.getMeals()) {
                DietMeal meal = DietMeal.builder()
                        .mealType(mealDto.getMealType())
                        .totalKcal(0)
                        .build();

                int mealKcal = 0;
                if (mealDto.getFoods() != null) {
                    for (DietRequestDto.FoodItemDto foodDto : mealDto.getFoods()) {
                        FoodItem food = FoodItem.builder()
                                .name(foodDto.getName())
                                .amount(foodDto.getAmount())
                                .kcal(foodDto.getKcal())
                                .carbs(foodDto.getCarbs())
                                .protein(foodDto.getProtein())
                                .fat(foodDto.getFat())
                                .build();
                        meal.addFoodItem(food);
                        mealKcal += (food.getKcal() != null ? food.getKcal() : 0);
                    }
                }
                meal.setTotalKcal(mealKcal);
                dietRecord.addMeal(meal);
                dailyTotalKcal += mealKcal;
            }
        }
        dietRecord.setTotalDailyKcal(dailyTotalKcal);

        DietRecord savedRecord = dietRecordRepository.save(dietRecord);
        return toResponseDto(savedRecord);
    }

    // 2. 식단 조회 (Entity -> DTO 변환)
    @Transactional(readOnly = true)
    public DietResponseDto getDietByUserAndDate(Long userId, LocalDate date) {
        DietRecord record = dietRecordRepository.findByUser_IdAndDietDate(userId, date)
                .orElseThrow(() -> new NoSuchElementException("해당 날짜의 식단이 없습니다."));
        return toResponseDto(record);
    }

    // 3. 식단 삭제
    @Transactional
    public void deleteDietByUserAndDate(Long userId, LocalDate date) {
        DietRecord record = dietRecordRepository.findByUser_IdAndDietDate(userId, date)
                .orElseThrow(() -> new NoSuchElementException("삭제할 식단이 없습니다."));
        dietRecordRepository.delete(record);
    }

    // [Helper] Entity -> Response DTO 변환 메서드
    private DietResponseDto toResponseDto(DietRecord record) {
        return DietResponseDto.builder()
                .id(record.getId())
                .date(record.getDietDate())
                .totalDailyKcal(record.getTotalDailyKcal())
                .meals(record.getMeals().stream().map(meal -> DietResponseDto.DietMealResponseDto.builder()
                        .mealType(meal.getMealType())
                        .totalKcal(meal.getTotalKcal())
                        .foods(meal.getFoodItems().stream().map(food -> DietResponseDto.FoodItemResponseDto.builder()
                                .name(food.getName())
                                .amount(food.getAmount())
                                .kcal(food.getKcal())
                                .carbs(food.getCarbs())
                                .protein(food.getProtein())
                                .fat(food.getFat())
                                .build()).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList()))
                .build();
    }
}