package com.Gym_PT.dto;

public record FatSecretImageRequest(
        String image_b64,
        boolean include_food_data
) {}