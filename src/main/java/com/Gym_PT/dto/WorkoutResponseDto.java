package com.Gym_PT.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class WorkoutResponseDto {

    private Long userId;

    /**
     * 📌 DB에 저장된 workoutDate(LocalDateTime)를 그대로 노출
     * 예: "2025-11-28T06:34:06"
     *
     * 프론트에서는 string으로 받아서 Date/텍스트 포맷팅해서 사용.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime date;

    private List<WorkoutRequestDto.RunningDto> running;
    private List<WorkoutRequestDto.NonWeightDto> nonWeight;
    private List<WorkoutRequestDto.WeightDto> weight;
}
