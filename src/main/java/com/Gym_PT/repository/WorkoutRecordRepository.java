package com.Gym_PT.repository;

import com.Gym_PT.entity.WorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkoutRecordRepository extends JpaRepository<WorkoutRecord, Long> {
    Optional<WorkoutRecord> findByUserIdAndWorkoutDate(Long userId, LocalDate workoutDate);
    Optional<WorkoutRecord> findTopByUser_IdOrderByWorkoutDateDesc(Long userId);
    List<WorkoutRecord> findTop7ByUser_IdOrderByWorkoutDateDesc(Long userId);
}
