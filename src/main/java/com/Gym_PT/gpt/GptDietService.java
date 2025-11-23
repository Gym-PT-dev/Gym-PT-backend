package com.Gym_PT.gpt;

import com.Gym_PT.entity.*;
import com.Gym_PT.repository.DietRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GptDietService {

    private final GptConfig gptConfig;
    private final RestTemplate restTemplate;
    private final DietRecordRepository dietRecordRepository;

    @Transactional(readOnly = true)
    public String generateDiet(Long userId, UserProfile profile) {
        List<DietRecord> recentDiets = dietRecordRepository.findTop7ByUser_IdOrderByDietDateDesc(userId);
        Collections.reverse(recentDiets);
        String recentLog = formatDietLogs(recentDiets);
        return callGptApi(profile, recentLog);
    }

    private String callGptApi(UserProfile profile, String recentLog) {
        // ▼▼▼ [프롬프트 강력 수정] ▼▼▼
        String systemPrompt = """
            너는 전문 영양 코치다. 사용자 정보와 최근 7일 식단을 보고 오늘 식단을 JSON으로 작성하라.
            
            [절대 규칙: 4끼 필수 포함]
            1. **무조건** 'BREAKFAST', 'LUNCH', 'DINNER', 'SNACK' 4가지 끼니를 모두 포함해야 한다. 하나라도 빠지면 안 된다.
            2. **메뉴 구성**:
               - **아침(BREAKFAST)**: 간단하지만 든든하게 (예: 샌드위치, 요거트볼, 한식 등)
               - **점심(LUNCH) & 저녁(DINNER)**: 반드시 '밥/탄수화물 + 메인반찬(단백질) + 채소' 조합으로 **3가지 이상의 음식**을 포함하라.
               - **간식(SNACK)**: 견과류, 과일, 프로틴바 등 1~2가지.
            3. 다양성: 최근 먹은 메뉴와 겹치지 않게 추천하라.
            
            [출력 구조 준수]
            {
              "meals": [
                {
                  "mealType": "BREAKFAST",
                  "foods": [
                     { "name": "사과", "amount": "1개", "kcal": 150, "carbs": 30, "protein": 1, "fat": 0 },
                     { "name": "삶은 계란", "amount": "2개", "kcal": 140, "carbs": 2, "protein": 12, "fat": 10 }
                  ]
                },
                {
                  "mealType": "LUNCH",
                  "foods": [ ... ]
                },
                {
                  "mealType": "DINNER",
                  "foods": [ ... ]
                },
                {
                  "mealType": "SNACK",
                  "foods": [ ... ]
                }
              ]
            }
            * mealType은 "BREAKFAST", "LUNCH", "DINNER", "SNACK" 4개가 전부 있어야 한다.
            * 숫자는 정수(Integer)로.
            """;

        String userPrompt = String.format("목표: %s\n최근식단: %s", profile.getGoal(), recentLog);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.6,
                "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptConfig.getApiKey());

        ResponseEntity<Map> response = restTemplate.postForEntity(gptConfig.getApiUrl(), new HttpEntity<>(requestBody, headers), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
    }

    private String formatDietLogs(List<DietRecord> records) {
        if (records.isEmpty()) return "기록 없음";
        StringBuilder sb = new StringBuilder();
        for (DietRecord r : records) {
            sb.append("[").append(r.getDietDate()).append("] ");
            r.getMeals().forEach(m -> {
                if(!m.getFoodItems().isEmpty()) sb.append(m.getMealType()).append(":").append(m.getFoodItems().get(0).getName()).append(" ");
            });
            sb.append("\n");
        }
        return sb.toString();
    }
}