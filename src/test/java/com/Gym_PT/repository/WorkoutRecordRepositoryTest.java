package com.Gym_PT.repository;

import com.Gym_PT.entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class WorkoutRecordRepositoryTest {

    @Autowired UsersRepository usersRepository;
    @Autowired WorkoutRecordRepository workoutRecordRepository;

    @Test
    void findByUserIdAndWorkoutDate_works_and_cascade_persist() {
        // given
        Users u = usersRepository.save(Users.builder().googleSub("sub-wr1").email("u@test.com").build());
        LocalDate d = LocalDate.of(2025, 8, 1);

        WorkoutRecord rec = new WorkoutRecord();
        rec.setUser(u);
        rec.setWorkoutDate(d);

        RunningRecord run = new RunningRecord();
        run.setRunningName("런닝머신"); run.setSpeed(9.5f); run.setDuration(20);
        run.setWorkoutRecord(rec); rec.getRunningRecords().add(run);

        WeightRecord w = new WeightRecord();
        w.setWeightName("벤치프레스"); w.setWeight(60); w.setWeightSets(4); w.setWeightReps(8);
        w.setWorkoutRecord(rec); rec.getWeightRecords().add(w);

        workoutRecordRepository.save(rec); // 자식까지 저장

        // when
        var found = workoutRecordRepository.findByUserIdAndWorkoutDate(u.getId(), d);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getRunningRecords()).hasSize(1);
        assertThat(found.get().getWeightRecords()).hasSize(1);
    }

    @Test
    void orphanRemoval_on_parent_delete_removes_children() {
        Users u = usersRepository.save(Users.builder().googleSub("sub-wr2").build());
        LocalDate d = LocalDate.of(2025, 8, 2);

        WorkoutRecord rec = new WorkoutRecord();
        rec.setUser(u); rec.setWorkoutDate(d);

        NonWeightRecord nw = new NonWeightRecord();
        nw.setNonWeightName("푸쉬업"); nw.setNonWeightSets(3); nw.setNonWeightReps(15);
        nw.setWorkoutRecord(rec); rec.getNonWeightRecords().add(nw);

        workoutRecordRepository.save(rec);

        // 삭제
        workoutRecordRepository.delete(rec);

        assertThat(workoutRecordRepository.findByUserIdAndWorkoutDate(u.getId(), d)).isEmpty();
    }
}
