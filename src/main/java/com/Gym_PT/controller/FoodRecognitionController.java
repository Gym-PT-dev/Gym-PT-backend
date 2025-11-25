package com.Gym_PT.controller;

import com.Gym_PT.dto.DietRequestDto;
import com.Gym_PT.entity.DietMeal.MealType;
import com.Gym_PT.imageupload.S3Uploader;
import com.Gym_PT.service.FatSecretConversionService;
import com.Gym_PT.service.FatSecretService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/food")
@RequiredArgsConstructor
public class FoodRecognitionController {

    private final S3Uploader s3Uploader;
    private final FatSecretService fatSecretService;
    private final FatSecretConversionService conversionService;

    /**
     * [이미지 분석 API]
     * 1. S3 업로드
     * 2. FatSecret API 호출
     * 3. 결과 포맷팅 (DietRequestDto)
     * 4. 반환 (DB 저장 X)
     */
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> recognizeFoodImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "mealType", defaultValue = "LUNCH") String mealTypeStr
    ) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("Image file is empty.");

        try {
            // 1. S3 업로드
            String s3Url = s3Uploader.uploadFile(file);
            System.out.println("[FoodRecognitionController] S3 URL = " + s3Url);

            // 2. FatSecret API 호출 (Raw JSON String 받기)
            String rawJson = fatSecretService.recognizeImageFromUrl(s3Url);
            System.out.println("[FoodRecognitionController] Raw FatSecret JSON = ");
            System.out.println(rawJson);

            // 3. Raw JSON -> 우리 서버 DTO 변환
            MealType mealType = MealType.valueOf(mealTypeStr.toUpperCase());
            DietRequestDto cleanResult = conversionService.convertToDietDto(rawJson, mealType);

            // 디버그: 파싱된 음식 개수
            int foodCount = 0;
            if (cleanResult.getMeals() != null && !cleanResult.getMeals().isEmpty()
                    && cleanResult.getMeals().get(0).getFoods() != null) {
                foodCount = cleanResult.getMeals().get(0).getFoods().size();
            }
            System.out.println("[FoodRecognitionController] Parsed foods count = " + foodCount);

            // 4. 결과 반환
            return ResponseEntity.ok(cleanResult);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}