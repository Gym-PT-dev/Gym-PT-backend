package com.Gym_PT.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FatSecretImageResponse(
        @JsonProperty("food_response") List<FoodResponseItem> foodResponse
) {}