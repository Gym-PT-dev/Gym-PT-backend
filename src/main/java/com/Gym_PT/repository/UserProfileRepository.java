package com.Gym_PT.repository;

import com.Gym_PT.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    // user(연관엔티티)의 id로 조회
    Optional<UserProfile> findByUser_Id(Long userId);
}