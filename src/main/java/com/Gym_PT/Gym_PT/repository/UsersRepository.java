package com.Gym_PT.Gym_PT.repository;

import com.Gym_PT.Gym_PT.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsersRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByGoogleSub(String googleSub);
    Optional<Users> findByEmail(String email);
    boolean existsByGoogleSub(String googleSub);
    boolean existsByEmail(String email);
}