// com/Gym_PT/config/DevDataSeeder.java
package com.Gym_PT.config;

import com.Gym_PT.entity.Users;
import com.Gym_PT.repository.UsersRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
public class DevDataSeeder implements CommandLineRunner {

    private final UsersRepository usersRepository;

    public DevDataSeeder(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public void run(String... args) {
        // admin 이메일 원하는 걸로 바꿔도 됨
        String adminEmail = "admin@gympt.dev";

        usersRepository.findByEmail(adminEmail).ifPresentOrElse(
                u -> { /* 이미 있으면 패스 */ },
                () -> {
                    Users admin = Users.builder()
                            .googleSub("DEV_ADMIN")       // 임시 식별자 (유니크 요구 충족)
                            .email(adminEmail)
                            .name("Dev Admin")
                            .picture(null)
                            .emailVerified(true)
                            .build();
                    usersRepository.save(admin);
                }
        );
    }
}
