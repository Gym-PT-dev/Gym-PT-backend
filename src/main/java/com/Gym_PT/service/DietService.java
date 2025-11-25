package com.Gym_PT.service;

import com.Gym_PT.dto.DietRequestDto;
import com.Gym_PT.dto.DietResponseDto;
import com.Gym_PT.entity.*;
import com.Gym_PT.entity.DietMeal.MealType;
import com.Gym_PT.repository.DietRecordRepository;
import com.Gym_PT.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DietService {

    private final DietRecordRepository dietRecordRepository;
    private final UsersRepository usersRepository;

    /**
     * ✅ 식단 저장 (부분 업데이트 버전)
     *  - 같은 날짜의 DietRecord 는 유지
     *  - 요청에 들어온 끼니(mealType)만 해당 DietMeal의 FoodItem들을 갈아끼움
     *  - 요청에 없는 끼니는 그대로 유지
     */
    @Transactional
    public DietResponseDto saveDiet(Long userId, DietRequestDto request) {
        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        LocalDate date = request.getDate();
        if (date == null) {
            throw new IllegalArgumentException("DietRequestDto.date is required");
        }

        // 1) user + date 기준으로 DietRecord 찾기 (없으면 새로 생성)
        DietRecord dietRecord = dietRecordRepository.findByUser_IdAndDietDate(userId, date)
                .orElseGet(() -> DietRecord.builder()
                        .user(user)
                        .dietDate(date)
                        .totalDailyKcal(0)
                        .build()
                );

        // 2) 요청에 끼니 정보가 없으면: 아무 것도 수정하지 않고 현재 상태 그대로 반환
        if (request.getMeals() == null || request.getMeals().isEmpty()) {
            DietRecord saved = dietRecordRepository.save(dietRecord);
            return toResponseDto(saved);
        }

        // 3) 기존 DietRecord 안의 DietMeal을 mealType 기준으로 맵핑
        Map<MealType, DietMeal> existingMealsByType = Optional.ofNullable(dietRecord.getMeals())
                .orElseGet(Collections::emptyList)
                .stream()
                .collect(Collectors.toMap(DietMeal::getMealType, m -> m));

        // 4) 요청된 각 끼니(mealType)에 대해 FoodItem 목록을 갈아끼움
        for (DietRequestDto.DietMealDto mealDto : request.getMeals()) {
            MealType mealType = mealDto.getMealType();
            if (mealType == null) continue;

            // 4-1) 기존 끼니가 있으면 가져오고, 없으면 새로 만든 후 DietRecord에 추가
            DietMeal meal = existingMealsByType.get(mealType);
            if (meal == null) {
                meal = DietMeal.builder()
                        .mealType(mealType)
                        .totalKcal(0)
                        .build();
                // DietRecord <-> DietMeal 연관관계 설정
                dietRecord.addMeal(meal);
                existingMealsByType.put(mealType, meal);
            }

            // 4-2) 기존 FoodItem 비우기 (orphanRemoval = true 가정)
            if (meal.getFoodItems() != null) {
                meal.getFoodItems().clear();
            }

            int mealKcal = 0;

            // 4-3) 새로운 FoodItem들 추가
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

                    meal.addFoodItem(food); // 연관관계 설정 (meal 세팅 + 리스트에 추가)
                    mealKcal += (food.getKcal() != null ? food.getKcal() : 0);
                }
            }

            // 4-4) 끼니별 총 kcal 갱신
            meal.setTotalKcal(mealKcal);
        }

        // 5) 모든 끼니 기준으로 하루 총 kcal 다시 계산
        int dailyTotalKcal = Optional.ofNullable(dietRecord.getMeals())
                .orElseGet(Collections::emptyList)
                .stream()
                .mapToInt(m -> Optional.ofNullable(m.getTotalKcal()).orElse(0))
                .sum();
        dietRecord.setTotalDailyKcal(dailyTotalKcal);

        // 6) 저장 후 ResponseDto로 변환
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
                .meals(record.getMeals().stream()
                        .map(meal -> DietResponseDto.DietMealResponseDto.builder()
                                .mealType(meal.getMealType())
                                .totalKcal(meal.getTotalKcal())
                                .foods(meal.getFoodItems().stream()
                                        .map(food -> DietResponseDto.FoodItemResponseDto.builder()
                                                .name(food.getName())
                                                .amount(food.getAmount())
                                                .kcal(food.getKcal())
                                                .carbs(food.getCarbs())
                                                .protein(food.getProtein())
                                                .fat(food.getFat())
                                                .build())
                                        .collect(Collectors.toList()))
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
