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

            // 2. FatSecret API 호출 (Raw JSON String 받기)
            String rawJson = fatSecretService.recognizeImageFromUrl(s3Url);

            // 3. Raw JSON -> 우리 서버 DTO 변환
            MealType mealType = MealType.valueOf(mealTypeStr.toUpperCase());
            DietRequestDto cleanResult = conversionService.convertToDietDto(rawJson, mealType);

            // 4. 결과 반환 (프론트엔드는 이 데이터를 보여주고, 수정 후 저장 API로 보냄)
            return ResponseEntity.ok(cleanResult);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}