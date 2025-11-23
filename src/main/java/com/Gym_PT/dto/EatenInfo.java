package com.Gym_PT.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EatenInfo(
        @JsonProperty("total_nutritional_content") Nutrition nutrition
) {}