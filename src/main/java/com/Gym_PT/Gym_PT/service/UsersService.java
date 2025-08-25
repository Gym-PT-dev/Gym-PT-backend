package com.Gym_PT.Gym_PT.service;

import com.Gym_PT.Gym_PT.entity.Users;
import com.Gym_PT.Gym_PT.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;

    @Transactional
    public Users upsertFromGoogle(String sub, String email, String name, String picture, Boolean emailVerified) {
        return usersRepository.findByGoogleSub(sub).map(u -> {
            if (email != null) u.setEmail(email);
            if (name != null) u.setName(name);
            if (picture != null) u.setPicture(picture);
            if (emailVerified != null) u.setEmailVerified(emailVerified);
            return u;
        }).orElseGet(() -> usersRepository.save(
                Users.builder()
                        .googleSub(sub)
                        .email(email)
                        .name(name)
                        .picture(picture)
                        .emailVerified(emailVerified)
                        .build()
        ));
    }

    @Transactional(readOnly = true)
    public Users getById(Long id) {
        return usersRepository.findById(id).orElseThrow();
    }
}
