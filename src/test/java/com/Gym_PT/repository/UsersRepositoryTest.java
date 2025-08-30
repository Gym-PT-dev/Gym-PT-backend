package com.Gym_PT.repository;

import com.Gym_PT.entity.Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UsersRepositoryTest {

    @Autowired UsersRepository usersRepository;

    @Test
    void save_and_findByGoogleSub() {
        Users u = Users.builder()
                .googleSub("sub-1")
                .email("a@test.com")
                .name("A")
                .build();
        usersRepository.save(u);

        Optional<Users> found = usersRepository.findByGoogleSub("sub-1");
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("a@test.com");
    }

    @Test
    void unique_googleSub_enforced() {
        usersRepository.save(Users.builder().googleSub("dup-sub").build());

        // 두 번째 저장이 바로 예외를 던질 수 있으므로 saveAndFlush로 검증
        assertThatThrownBy(() ->
                usersRepository.saveAndFlush(Users.builder().googleSub("dup-sub").build())
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
