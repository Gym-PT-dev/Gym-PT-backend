package com.Gym_PT.Gym_PT.service;

import com.Gym_PT.Gym_PT.dto.UserProfileDto;
import com.Gym_PT.Gym_PT.dto.UserProfileRequest;
import com.Gym_PT.Gym_PT.entity.UserProfile;
import com.Gym_PT.Gym_PT.entity.Users;
import com.Gym_PT.Gym_PT.repository.UserProfileRepository;
import com.Gym_PT.Gym_PT.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserProfileRepository profileRepo;
    private final UsersRepository usersRepo;

    @Transactional
    public UserProfileDto upsert(Long userId, UserProfileRequest payload) {
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        UserProfile profile = profileRepo.findByUserId(userId)
                .orElseGet(() -> UserProfile.builder().user(user).build());

        profile.setAge(payload.age());
        profile.setGender(payload.gender());
        profile.setHeight(payload.height());
        profile.setWeight(payload.weight());
        profile.setGoal(payload.goal());
        profile.setExperienceLevel(payload.experienceLevel());
        profile.setDuration(payload.duration());
        profile.setLocation(payload.location());
        profile.setSplitStrategy(payload.splitStrategy());

        UserProfile saved = profileRepo.save(profile);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<UserProfileDto> getByUserId(Long userId) {
        return profileRepo.findByUserId(userId).map(this::toDto);
    }

    private UserProfileDto toDto(UserProfile p) {
        return new UserProfileDto(
                p.getId(),
                p.getUser() != null ? p.getUser().getId() : null,
                p.getAge(),
                p.getGender() != null ? p.getGender().name() : null,
                p.getHeight(),
                p.getWeight(),
                p.getGoal() != null ? p.getGoal().name() : null,
                p.getExperienceLevel() != null ? p.getExperienceLevel().name() : null,
                p.getDuration(),
                p.getLocation(),
                p.getSplitStrategy()
        );
    }

    /** GPT 등 내부 서비스에서 엔티티 필요 시 사용 */
    @Transactional(readOnly = true)
    public UserProfile getEntityByUserIdOrThrow(Long userId) {
        return profileRepo.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("UserProfile not found for userId: " + userId));
    }
}
