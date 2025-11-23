package com.Gym_PT.gpt;

import com.Gym_PT.entity.*;
import com.Gym_PT.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GptRoutineService {

    private final GptConfig gptConfig;
    private final RestTemplate restTemplate;
    private final WorkoutRecordRepository workoutRecordRepository;

    @Transactional(readOnly = true)
    public String generateRoutine(Users user, UserProfile profile) {
        Long userId = (user.getId() != null) ? user.getId() : profile.getUser().getId();

        // 1. 최근 7일 기록 조회 & 시간순 정렬 (과거 -> 현재)
        List<WorkoutRecord> recentRecords = workoutRecordRepository.findTop7ByUser_IdOrderByWorkoutDateDesc(userId);
        Collections.reverse(recentRecords);

        // 2. 프롬프트 생성
        Map<String, Object> systemMessage = Map.of(
                "role", "system",
                "content", "당신은 피트니스 코치입니다. 최근 7일 기록을 분석해 분할과 점진적 과부하를 적용한 루틴을 JSON으로 짜주세요."
        );

        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", buildUserPrompt(profile, recentRecords)
        );

        return callGptApi(List.of(systemMessage, userMessage));
    }

    private String buildUserPrompt(UserProfile profile, List<WorkoutRecord> records) {
        StringBuilder sb = new StringBuilder();
        sb.append("사용자 정보와 최근 7일 기록을 바탕으로 루틴을 짜주세요.\n\n");

        sb.append("<PAST_LOGS>\n");
        if (records.isEmpty()) sb.append("기록 없음\n");
        for (WorkoutRecord r : records) {
            sb.append(String.format("[%s] ", r.getWorkoutDate()));
            r.getWeightRecords().forEach(w -> sb.append(w.getWeightName()).append(" ").append(w.getWeight()).append("kg, "));
            sb.append("\n");
        }
        sb.append("</PAST_LOGS>\n\n");

        sb.append("<OUTPUT_JSON_EXAMPLE>\n");
        sb.append("반드시 아래 Key 이름을 정확히 지켜주세요:\n");
        sb.append("""
        {
          "running": [
             { "runningName": "러닝", "speed": 6.0, "duration": 10 }
          ],
          "nonWeight": [
             { "nonWeightName": "푸쉬업", "nonWeightSets": 3, "nonWeightReps": 15 }
          ],
          "weight": [
             { "weightName": "벤치프레스", "weight": 60, "weightSets": 4, "weightReps": 10 }
          ]
        }
        """);
        sb.append("\n</OUTPUT_JSON_EXAMPLE>\n");
        return sb.toString();
    }

    private String callGptApi(List<Map<String, Object>> messages) {
        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4o",
                "messages", messages,
                "temperature", 0.7,
                "response_format", Map.of("type", "json_object")
        );
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(gptConfig.getApiKey());

        ResponseEntity<Map> response = restTemplate.postForEntity(gptConfig.getApiUrl(), new HttpEntity<>(requestBody, headers), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
        return (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");
    }
}