package com.Gym_PT.repository;

import com.Gym_PT.entity.WorkoutRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkoutRecordRepository extends JpaRepository<WorkoutRecord, Long> {

    //  특정 시각의 기록 1개 (필요 시 사용) - 기존 LocalDate → LocalDateTime
    Optional<WorkoutRecord> findByUserIdAndWorkoutDate(Long userId, LocalDateTime workoutDate);

    //  가장 최근 운동 기록 (정렬 기준: 날짜+시간 내림차순)
    Optional<WorkoutRecord> findTopByUser_IdOrderByWorkoutDateDesc(Long userId);

    //  최근 7개 운동 기록
    List<WorkoutRecord> findTop7ByUser_IdOrderByWorkoutDateDesc(Long userId);

    //  [중요] 하루 단위 조회/삭제용: 해당 날짜의 00:00 ~ 다음날 00:00 전까지
    List<WorkoutRecord> findAllByUser_IdAndWorkoutDateBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );
}
