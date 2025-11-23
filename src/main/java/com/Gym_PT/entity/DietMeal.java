package com.Gym_PT.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietMeal {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // [변경됨] Plan -> Record
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_record_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private DietRecord dietRecord;

    @Enumerated(EnumType.STRING)
    private MealType mealType; // BREAKFAST, LUNCH, DINNER, SNACK

    private Integer totalKcal;

    @OneToMany(mappedBy = "dietMeal", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FoodItem> foodItems = new ArrayList<>();

    public void addFoodItem(FoodItem foodItem) {
        this.foodItems.add(foodItem);
        foodItem.setDietMeal(this);
    }

    public enum MealType {
        BREAKFAST, LUNCH, DINNER, SNACK
    }
}