package com.Gym_PT.Gym_PT.service;

import com.Gym_PT.Gym_PT.entity.Users;
import com.Gym_PT.Gym_PT.repository.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(UsersService.class)
@ActiveProfiles("test")
class UsersServiceJpaTest {

    @Autowired UsersService usersService;
    @Autowired UsersRepository usersRepository;

    @Test
    void createOrGetByGoogleSub_saves_first_time_and_returns_existing_next_time() {
        Users first = Users.builder().googleSub("sub-xyz").email("x@test.com").name("X").build();
        Users saved1 = usersService.createOrGetByGoogleSub(first);

        Users secondWithSameSub = Users.builder().googleSub("sub-xyz").email("y@test.com").name("Y").build();
        Users saved2 = usersService.createOrGetByGoogleSub(secondWithSameSub);

        assertThat(saved1.getId()).isEqualTo(saved2.getId());
        assertThat(usersRepository.findAll()).hasSize(1);
    }

    @Test
    void deleteUser_removes_row() {
        Users u = usersService.createOrGetByGoogleSub(Users.builder().googleSub("sub-del").build());
        Long id = u.getId();
        usersService.deleteUser(id);
        assertThat(usersRepository.findById(id)).isEmpty();
    }
}
