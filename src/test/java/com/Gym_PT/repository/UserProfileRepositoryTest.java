package com.Gym_PT.repository;

import com.Gym_PT.entity.UserProfile;
import com.Gym_PT.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserProfileRepositoryTest {

    @Autowired UsersRepository usersRepository;
    @Autowired UserProfileRepository userProfileRepository;

    @Test
    void findByUserId_returns_profile() {
        Users u = usersRepository.save(Users.builder().googleSub("sub-prof-1").email("a@test.com").build());

        UserProfile p = UserProfile.builder()
                .user(u)
                .age(28)
                .gender(UserProfile.Gender.MALE)
                .height(178.5f)
                .weight(76.2f)
                .goal(UserProfile.Goal.DIET)
                .experienceLevel(UserProfile.ExperienceLevel.INTERMEDIATE)
                .duration(300)
                .location("Gym")
                .splitStrategy("PPL")
                .build();
        userProfileRepository.save(p);

        assertThat(userProfileRepository.findByUserId(u.getId()))
                .isPresent()
                .get()
                .extracting(UserProfile::getAge)
                .isEqualTo(28);
    }

    @Test
    void unique_user_id_enforced() {
        Users u = usersRepository.save(Users.builder().googleSub("sub-prof-uniq").build());

        userProfileRepository.save(UserProfile.builder().user(u).build());
        assertThatThrownBy(() ->
                userProfileRepository.saveAndFlush(UserProfile.builder().user(u).build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
