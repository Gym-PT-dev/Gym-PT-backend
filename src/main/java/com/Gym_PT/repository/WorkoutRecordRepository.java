package com.Gym_PT.repository;

import com.Gym_PT.entity.WorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface WorkoutRecordRepository extends JpaRepository<WorkoutRecord, Long> {

    List<WorkoutRecord> findAllByUser_IdAndWorkoutDate(Long userId, LocalDate workoutDate);
}
