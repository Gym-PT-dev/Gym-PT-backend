package com.Gym_PT.service;

import com.Gym_PT.entity.Users;
import com.Gym_PT.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service @RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;

    @Transactional
    public Users createOrGetByGoogleSub(Users user) {
        return usersRepository.findByGoogleSub(user.getGoogleSub())
                .orElseGet(() -> usersRepository.save(user));
    }

    public Optional<Users> getUser(Long id) { return usersRepository.findById(id); }
    public List<Users> getAll() { return usersRepository.findAll(); }

    @Transactional
    public void deleteUser(Long id) { usersRepository.deleteById(id); }
}
