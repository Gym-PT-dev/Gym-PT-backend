package com.Gym_PT.repository;

import com.Gym_PT.entity.DietRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DietRecordRepository extends JpaRepository<DietRecord, Long> {
    // 특정 유저의 특정 날짜 식단 기록 조회
    Optional<DietRecord> findByUser_IdAndDietDate(Long userId, LocalDate date);

    // 특정 유저의 모든 식단 기록 조회
    List<DietRecord> findAllByUser_Id(Long userId);
    List<DietRecord> findTop7ByUser_IdOrderByDietDateDesc(Long userId);
}