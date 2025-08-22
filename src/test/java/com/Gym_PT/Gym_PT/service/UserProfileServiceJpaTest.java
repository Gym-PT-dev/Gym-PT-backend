package com.Gym_PT.Gym_PT.service;

import com.Gym_PT.Gym_PT.entity.UserProfile;
import com.Gym_PT.Gym_PT.entity.Users;
import com.Gym_PT.Gym_PT.repository.UserProfileRepository;
import com.Gym_PT.Gym_PT.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UserProfileService.class)
@ActiveProfiles("test")
class UserProfileServiceJpaTest {

    @Autowired UsersRepository usersRepository;
    @Autowired UserProfileRepository profileRepository;
    @Autowired UserProfileService userProfileService;

    @Test
    void upsert_create_then_update_keeps_single_row() {
        Users u = usersRepository.save(Users.builder().googleSub("sub-upsert").email("x@test.com").build());

        // create
        UserProfile payload1 = UserProfile.builder()
                .age(25)
                .gender(UserProfile.Gender.FEMALE)
                .height(165f)
                .weight(55f)
                .goal(UserProfile.Goal.KEEP)
                .experienceLevel(UserProfile.ExperienceLevel.BEGINNER)
                .duration(180)
                .location("Home")
                .splitStrategy("FBW")
                .build();

        UserProfile created = userProfileService.upsert(u.getId(), payload1);
        assertThat(created.getId()).isNotNull();
        assertThat(profileRepository.findByUserId(u.getId())).isPresent();

        // update
        UserProfile payload2 = UserProfile.builder()
                .age(26) // 변경
                .height(166f)
                .build();

        UserProfile updated = userProfileService.upsert(u.getId(), payload2);
        assertThat(updated.getAge()).isEqualTo(26);
        assertThat(updated.getHeight()).isEqualTo(166f);

        // 여전히 1건
        assertThat(profileRepository.count()).isEqualTo(1);
    }
}
