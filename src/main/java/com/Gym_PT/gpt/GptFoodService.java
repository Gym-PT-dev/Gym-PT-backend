package com.Gym_PT.gpt;

import com.Gym_PT.dto.FoodAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GptFoodService {

    private final GptConfig gptConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public FoodAnalysisResponse analyzeFood(String foodName, String amount) {

        // 1. 프롬프트 작성
        String systemPrompt = """
            당신은 정밀한 영양 성분 분석가입니다.
            사용자가 음식 이름과 양을 입력하면, 해당 음식의 칼로리와 탄수화물, 단백질, 지방 함량을 추정하여 JSON으로 반환하세요.
            
            [규칙]
            1. 한국인 영양 섭취 기준 또는 일반적인 데이터베이스를 참고할 것.
            2. 수치는 정수(Integer)로 반올림할 것.
            3. 양(amount)이 불명확하면 '1인분'을 기준으로 추정할 것.
            
            [출력 JSON 형식]
            {
                "name": "음식명",
                "amount": "요청받은 양",
                "kcal": 0,
                "carbs": 0,
                "protein": 0,
                "fat": 0
            }
            """;

        String userPrompt = String.format("음식: %s, 양: %s", foodName, amount);

        // 2. GPT 요청 본문 구성
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o-mini", // 가볍고 빠른 모델 사용
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "temperature", 0.3, // 정확도를 위해 낮게 설정
                "response_format", Map.of("type", "json_object")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptConfig.getApiKey());

        // 3. API 호출
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    gptConfig.getApiUrl(),
                    new HttpEntity<>(requestBody, headers),
                    Map.class
            );

            // 4. 응답 파싱
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            String content = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");

            // JSON String -> DTO 변환
            return objectMapper.readValue(content, FoodAnalysisResponse.class);

        } catch (Exception e) {
            throw new RuntimeException("GPT 음식 분석 실패: " + e.getMessage(), e);
        }
    }
}