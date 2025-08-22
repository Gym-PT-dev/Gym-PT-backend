package com.Gym_PT.Gym_PT.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "DailyWorkoutLogs")
public class DailyWorkoutLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    private LocalDate date;

    @Column(name = "goal_rate")
    private Float goalRate;

    @Column(name = "total_time")
    private Float totalTime;
}
