package com.Gym_PT.Gym_PT.service;

import com.Gym_PT.Gym_PT.entity.UserProfile;
import com.Gym_PT.Gym_PT.entity.Users;
import com.Gym_PT.Gym_PT.repository.UserProfileRepository;
import com.Gym_PT.Gym_PT.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository profileRepo;
    private final UsersRepository usersRepo;

    @Transactional
    public UserProfile upsert(Long userId, UserProfile payload) {
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        Optional<UserProfile> existing = profileRepo.findByUserId(userId);

        UserProfile profile = existing.orElseGet(() ->
                UserProfile.builder().user(user).build()
        );

        // 필요한 필드 업데이트
        profile.setAge(payload.getAge());
        profile.setGender(payload.getGender());
        profile.setHeight(payload.getHeight());
        profile.setWeight(payload.getWeight());
        profile.setGoal(payload.getGoal());
        profile.setExperienceLevel(payload.getExperienceLevel());
        profile.setDuration(payload.getDuration());
        profile.setLocation(payload.getLocation());
        profile.setSplitStrategy(payload.getSplitStrategy());

        return profileRepo.save(profile);
    }

    public Optional<UserProfile> getByUserId(Long userId) {
        return profileRepo.findByUserId(userId);
    }
}
