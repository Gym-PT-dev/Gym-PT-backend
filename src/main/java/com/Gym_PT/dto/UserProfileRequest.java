package com.Gym_PT.dto;

import com.Gym_PT.entity.UserProfile.ExperienceLevel;
import com.Gym_PT.entity.UserProfile.Gender;
import com.Gym_PT.entity.UserProfile.Goal;

public record UserProfileRequest(
        Integer age,
        Gender gender,
        Float height,
        Float weight,
        Goal goal,
        ExperienceLevel experienceLevel,
        Integer duration,
        String location,
        String splitStrategy
) {}
