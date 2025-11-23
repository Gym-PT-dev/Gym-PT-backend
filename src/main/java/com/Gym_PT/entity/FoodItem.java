package com.Gym_PT.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_meal_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DietMeal dietMeal;

    private String name;    // 음식명
    private String amount;  // 양

    private Integer kcal;
    private Integer carbs;
    private Integer protein;
    private Integer fat;
}