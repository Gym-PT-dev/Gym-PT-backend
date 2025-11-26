package com.Gym_PT.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
public class WorkoutRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 날짜 + 시간 저장용
    private LocalDateTime workoutDate;  // ← 변경됨!

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    @OneToMany(mappedBy = "workoutRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RunningRecord> runningRecords = new ArrayList<>();

    @OneToMany(mappedBy = "workoutRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NonWeightRecord> nonWeightRecords = new ArrayList<>();

    @OneToMany(mappedBy = "workoutRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WeightRecord> weightRecords = new ArrayList<>();
}
