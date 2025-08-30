package com.Gym_PT.service;

import com.Gym_PT.dto.WorkoutRequestDto;
import com.Gym_PT.dto.WorkoutResponseDto;
import com.Gym_PT.entity.Users;
import com.Gym_PT.repository.UsersRepository;
import com.Gym_PT.repository.WorkoutRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(WorkoutService.class)
@ActiveProfiles("test")
class WorkoutServiceJpaTest {

    @Autowired WorkoutService workoutService;
    @Autowired UsersRepository usersRepository;
    @Autowired WorkoutRecordRepository workoutRecordRepository;

    @Test
    @DisplayName("save → get → delete 전체 플로우")
    void save_get_delete_flow() {
        // given
        Users user = usersRepository.save(Users.builder().googleSub("sub-svc1").email("a@test.com").build());
        LocalDate date = LocalDate.of(2025, 8, 3);

        WorkoutRequestDto req = new WorkoutRequestDto();
        req.setDate(date);

        var r = new WorkoutRequestDto.RunningDto();
        r.setRunningName("런닝"); r.setSpeed(9.0f); r.setDuration(15);

        var n = new WorkoutRequestDto.NonWeightDto();
        n.setNonWeightName("스쿼트"); n.setNonWeightSets(3); n.setNonWeightReps(20);

        var w = new WorkoutRequestDto.WeightDto();
        w.setWeightName("데드리프트"); w.setWeight(80); w.setWeightSets(3); w.setWeightReps(5);

        req.setRunning(List.of(r));
        req.setNonWeight(List.of(n));
        req.setWeight(List.of(w));

        // when: save
        workoutService.saveWorkout(user.getId(), req);

        // then: get
        WorkoutResponseDto found = workoutService.getWorkoutByUserAndDate(user.getId(), date);
        assertThat(found.getUserId()).isEqualTo(user.getId());
        assertThat(found.getDate()).isEqualTo(date);
        assertThat(found.getRunning()).hasSize(1);
        assertThat(found.getNonWeight()).hasSize(1);
        assertThat(found.getWeight()).hasSize(1);

        // and: delete
        workoutService.deleteWorkoutByUserAndDate(user.getId(), date);
        assertThat(workoutRecordRepository.findByUserIdAndWorkoutDate(user.getId(), date)).isEmpty();
    }
}
