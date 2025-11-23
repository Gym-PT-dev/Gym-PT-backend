package com.Gym_PT.repository;

import com.Gym_PT.entity.WorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

public interface WorkoutRecordRepository extends JpaRepository<WorkoutRecord, Long> {
    Optional<WorkoutRecord> findByUserIdAndWorkoutDate(Long userId, LocalDate workoutDate);

    List<WorkoutRecord> findAllByUser_IdAndWorkoutDate(Long userId, LocalDate workoutDate);
}