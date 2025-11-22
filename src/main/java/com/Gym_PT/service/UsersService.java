package com.Gym_PT.service;

import com.Gym_PT.entity.Users;
import com.Gym_PT.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;

    @Transactional
    public Users upsertFromGoogle(String sub, String email, String name, String picture, Boolean emailVerified) {
        return usersRepository.findByGoogleSub(sub)
                .map(u -> {
                    // 🔹 기존 유저: 구글에서 온 값들만 업데이트
                    if (email != null) u.setEmail(email);
                    if (name != null) u.setName(name);
                    if (picture != null) u.setPicture(picture);
                    if (emailVerified != null) u.setEmailVerified(emailVerified);

                    // 🔹 예전 계정이라 displayName이 비어 있는 경우 한 번만 초기화
                    if ((u.getDisplayName() == null || u.getDisplayName().isBlank()) && name != null) {
                        u.setDisplayName(name);
                    }

                    return u;
                })
                .orElseGet(() -> {
                    Users newUser = Users.builder()
                            .googleSub(sub)
                            .email(email)
                            .name(name)
                            .picture(picture)
                            .emailVerified(emailVerified)
                            .displayName(name)   // ⭐ 여기!
                            .build();
                    return usersRepository.save(newUser);
                });
    }

    @Transactional(readOnly = true)
    public Users getById(Long id) {
        return usersRepository.findById(id).orElseThrow();
    }
}
