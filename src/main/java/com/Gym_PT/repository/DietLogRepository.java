package com.Gym_PT.repository;

import com.Gym_PT.entity.DietLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DietLogRepository extends JpaRepository<DietLog, Long> {
    List<DietLog> findByUserId(Long userId);
}