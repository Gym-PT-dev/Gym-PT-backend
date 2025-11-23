package com.Gym_PT.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @Column(nullable = false)
    private LocalDate dietDate; // 식단 날짜

    @CreationTimestamp
    private LocalDate createdAt; // 생성일 (기록일)

    // 하루 총 칼로리
    private Integer totalDailyKcal;

    // 끼니 리스트 (Record와 연결)
    @OneToMany(mappedBy = "dietRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DietMeal> meals = new ArrayList<>();

    // 연관관계 편의 메서드
    public void addMeal(DietMeal meal) {
        this.meals.add(meal);
        meal.setDietRecord(this);
    }
}