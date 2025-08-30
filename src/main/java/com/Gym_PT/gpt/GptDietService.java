package com.Gym_PT.gpt;

import com.Gym_PT.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GptDietService {

    private final GptConfig gptConfig;

    /** 컨트롤러에서 프로필을 넘겨 받도록 변경 */
    public String generateDiet(UserProfile profile) {
        int age = profile.getAge() == null ? 0 : profile.getAge();
        String gender = profile.getGender() == null ? "" : profile.getGender().name(); // "MALE" | "FEMALE"
        float height = profile.getHeight() == null ? 0f : profile.getHeight();
        float weight = profile.getWeight() == null ? 0f : profile.getWeight();
        String goal = profile.getGoal() == null ? "" : profile.getGoal().name();       // "DIET" | "KEEP" | "BULK"
        return generateDiet(age, gender, height, weight, goal);
    }

    /** 내부 핵심 호출(필요 시 재사용) */
    public String generateDiet(int age, String gender, float height, float weight, String goal) {

        String systemPrompt = """
            너는 한국인 대상의 전문 영양 코치다.
            아래 규칙을 반드시 지켜서 "JSON만" 출력하라. 불필요한 설명/마크다운/코드블록은 출력 금지.

            [계산 규칙]
            1) 칼로리(단순화 계수):
               - 유지: 30 × 체중(kg)
               - 다이어트: 26 × 체중(kg)
               - 증량: 34 × 체중(kg)
               ※ 사용한 계수를 targets.method_note에 명시.

            2) 단백질/지방/탄수화물(체중 기반):
               - 단백질:
                 · 다이어트: 2.2 g/kg
                 · 유지:     1.8 g/kg
                 · 증량:     1.7 g/kg
               - 지방: 0.9 g/kg
               - 탄수화물: 남은 칼로리로 계산
                   carbs_g = (총칼로리 - (protein_g×4 + fat_g×9)) ÷ 4
               - 모든 g 수치는 반올림하여 정수로 제시.

            3) 추천 식단(한국식 재료 중심, 3끼+간식 = 총 4끼):
               - 각 끼니: 음식명, 1회 제공량(g/개수), kcal, 탄/단/지 g
               - 총합이 목표 칼로리 ±5% 이내
               - 단백질은 끼니마다 고르게 분배
               - 실용적 재료 사용

            4) 수분 섭취:
               - 체중 × 30~35 ml/일 (정수 반올림)

            [출력 형식(JSON만)]
            {
              "user": { "sex": "남|여", "height_cm": 0, "weight_kg": 0, "goal": "다이어트|유지|증량" },
              "targets": { "method_note": "", "calories_kcal": 0, "protein_g": 0, "fat_g": 0, "carbs_g": 0 },
              "meal_plan": {
                "meals_per_day": 4,
                "meals": [
                  { "name": "아침", "items": [], "subtotal": {"kcal":0,"carbs_g":0,"protein_g":0,"fat_g":0} },
                  { "name": "점심", "items": [], "subtotal": {"kcal":0,"carbs_g":0,"protein_g":0,"fat_g":0} },
                  { "name": "간식", "items": [], "subtotal": {"kcal":0,"carbs_g":0,"protein_g":0,"fat_g":0} },
                  { "name": "저녁", "items": [], "subtotal": {"kcal":0,"carbs_g":0,"protein_g":0,"fat_g":0} }
                ],
                "total": {"kcal":0,"carbs_g":0,"protein_g":0,"fat_g":0}
              },
              "hydration": { "recommended_ml_per_day": 0, "note": "체중×30~35 ml 기준" },
              "tips": ["..."]
            }

            [중요]
            - 반드시 유효한 JSON만 출력.
            - kcal/탄/단/지는 정수.
            """;

        String userJson = String.format(Locale.ROOT, """
            {
              "user": {
                "gender": "%s",
                "height_cm": %.1f,
                "weight_kg": %.1f,
                "goal": "%s",
                "age": %d
              }
            }
            """,
                safeUpper(gender), height, weight, safeUpper(goal), age
        );

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(gptConfig.getApiKey());

        Map<String, Object> systemMsg = Map.of("role", "system", "content", systemPrompt);
        Map<String, Object> userMsg   = Map.of("role", "user",   "content", userJson);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o-mini");
        requestBody.put("messages", List.of(systemMsg, userMsg));
        requestBody.put("temperature", 0.3);
        requestBody.put("max_tokens", 2000);
        requestBody.put("response_format", Map.of("type", "json_object")); // JSON 강제(지원 모델만)

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions",
                    entity,
                    Map.class
            );
        } catch (Exception e) {
            throw new RuntimeException("OpenAI 호출 실패: " + e.getMessage(), e);
        }

        Map body = response.getBody();
        if (body == null) throw new IllegalStateException("응답 본문이 비어있습니다.");

        Object choicesObj = body.get("choices");
        if (!(choicesObj instanceof List) || ((List<?>) choicesObj).isEmpty()) {
            throw new IllegalStateException("유효한 choices가 없습니다: " + choicesObj);
        }

        Object firstChoice = ((List<?>) choicesObj).get(0);
        if (!(firstChoice instanceof Map)) {
            throw new IllegalStateException("choice 형식이 잘못되었습니다: " + firstChoice);
        }

        Object messageObj = ((Map<?, ?>) firstChoice).get("message");
        if (!(messageObj instanceof Map)) {
            throw new IllegalStateException("message 필드가 없습니다: " + messageObj);
        }

        Object contentObj = ((Map<?, ?>) messageObj).get("content");
        if (!(contentObj instanceof String)) {
            throw new IllegalStateException("content 필드가 없습니다: " + contentObj);
        }

        return (String) contentObj;
    }

    private static String safeUpper(String s) {
        return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
    }
}
