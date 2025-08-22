package com.Gym_PT.Gym_PT.dto;

public record UserProfileDto(
        Long id,
        Long userId,
        Integer age,
        String gender,
        Float height,
        Float weight,
        String goal,
        String experienceLevel,
        Integer duration,
        String location,
        String splitStrategy
) {}