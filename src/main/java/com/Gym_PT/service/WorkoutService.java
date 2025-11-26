package com.Gym_PT.service;

import com.Gym_PT.dto.WorkoutRequestDto;
import com.Gym_PT.dto.WorkoutResponseDto;
import com.Gym_PT.entity.NonWeightRecord;
import com.Gym_PT.entity.RunningRecord;
import com.Gym_PT.entity.Users;
import com.Gym_PT.entity.WeightRecord;
import com.Gym_PT.entity.WorkoutRecord;
import com.Gym_PT.repository.UsersRepository;
import com.Gym_PT.repository.WorkoutRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final UsersRepository usersRepository;
    private final WorkoutRecordRepository workoutRecordRepository;

    /**
     * 🔹 운동 저장
     *  - 요청 DTO는 LocalDate(date)만 가지지만
     *  - DB에는 LocalDateTime(날짜 + 현재 시간)으로 저장
     */
    @Transactional
    public WorkoutRequestDto saveWorkout(Long userId, WorkoutRequestDto dto) {

        Users user = usersRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        WorkoutRecord workoutRecord = new WorkoutRecord();
        workoutRecord.setUser(user);

        // DTO(LocalDate) + 현재 시간 → LocalDateTime
        LocalDate dtoDate = dto.getDate();
        LocalDateTime workoutDateTime =
                (dtoDate != null)
                        ? dtoDate.atTime(LocalTime.now())
                        : LocalDateTime.now();

        workoutRecord.setWorkoutDate(workoutDateTime);

        // Running
        if (dto.getRunning() != null) {
            for (WorkoutRequestDto.RunningDto r : dto.getRunning()) {
                RunningRecord run = new RunningRecord();
                run.setRunningName(r.getRunningName());
                run.setSpeed(r.getSpeed());
                run.setDuration(r.getDuration());
                run.setWorkoutRecord(workoutRecord);
                workoutRecord.getRunningRecords().add(run);
            }
        }

        // NonWeight
        if (dto.getNonWeight() != null) {
            for (WorkoutRequestDto.NonWeightDto n : dto.getNonWeight()) {
                NonWeightRecord non = new NonWeightRecord();
                non.setNonWeightName(n.getNonWeightName());
                non.setNonWeightSets(n.getNonWeightSets());
                non.setNonWeightReps(n.getNonWeightReps());
                non.setWorkoutRecord(workoutRecord);
                workoutRecord.getNonWeightRecords().add(non);
            }
        }

        // Weight
        if (dto.getWeight() != null) {
            for (WorkoutRequestDto.WeightDto w : dto.getWeight()) {
                WeightRecord weight = new WeightRecord();
                weight.setWeightName(w.getWeightName());
                weight.setWeightSets(w.getWeightSets());
                weight.setWeightReps(w.getWeightReps());
                weight.setWeight(w.getWeight());
                weight.setWorkoutRecord(workoutRecord);
                workoutRecord.getWeightRecords().add(weight);
            }
        }

        workoutRecordRepository.save(workoutRecord);
        return dto;
    }

    /**
     * 🔹 같은 날짜에 WorkoutRecord가 여러 개 있을 수 있다고 가정하고
     *    → List<WorkoutResponseDto> 로 전부 반환
     *
     *   - 컨트롤러에서 LocalDate로 date를 받음
     *   - 여기서 해당 날짜의 00:00 ~ 다음날 00:00 범위로 LocalDateTime 조회
     *   - 응답 DTO의 date는 LocalDateTime 그대로 내려보냄
     */
    @Transactional(readOnly = true)
    public List<WorkoutResponseDto> getWorkoutListByUserAndDate(Long userId, LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        // 🔥 레포지토리는 findAllByUser_IdAndWorkoutDateBetween(...) 으로 구현되어 있어야 함
        List<WorkoutRecord> records =
                workoutRecordRepository.findAllByUser_IdAndWorkoutDateBetween(userId, startOfDay, endOfDay);

        List<WorkoutResponseDto> result = new ArrayList<>();

        for (WorkoutRecord record : records) {
            WorkoutResponseDto dto = new WorkoutResponseDto();
            dto.setUserId(userId);

            // ❌ 예전: dto.setDate(record.getWorkoutDate().toLocalDate());
            // ✅ 지금: LocalDateTime 그대로 넘김
            dto.setDate(record.getWorkoutDate());

            // Running 매핑
            dto.setRunning(record.getRunningRecords().stream().map(r -> {
                WorkoutRequestDto.RunningDto d = new WorkoutRequestDto.RunningDto();
                d.setRunningName(r.getRunningName());
                d.setSpeed(r.getSpeed());
                d.setDuration(r.getDuration());
                return d;
            }).toList());

            // NonWeight 매핑
            dto.setNonWeight(record.getNonWeightRecords().stream().map(n -> {
                WorkoutRequestDto.NonWeightDto d = new WorkoutRequestDto.NonWeightDto();
                d.setNonWeightName(n.getNonWeightName());
                d.setNonWeightSets(n.getNonWeightSets());
                d.setNonWeightReps(n.getNonWeightReps());
                return d;
            }).toList());

            // Weight 매핑
            dto.setWeight(record.getWeightRecords().stream().map(w -> {
                WorkoutRequestDto.WeightDto d = new WorkoutRequestDto.WeightDto();
                d.setWeightName(w.getWeightName());
                d.setWeightSets(w.getWeightSets());
                d.setWeightReps(w.getWeightReps());
                d.setWeight(w.getWeight());
                return d;
            }).toList());

            result.add(dto);
        }

        return result;
    }

    /**
     * 🔹 해당 날짜의 WorkoutRecord들을 전부 삭제
     *   - 조회와 동일하게 00:00 ~ 다음날 00:00 범위로 삭제
     */
    @Transactional
    public void deleteWorkoutByUserAndDate(Long userId, LocalDate date) {

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<WorkoutRecord> records =
                workoutRecordRepository.findAllByUser_IdAndWorkoutDateBetween(userId, startOfDay, endOfDay);

        if (records.isEmpty()) {
            throw new IllegalArgumentException("운동 기록이 존재하지 않습니다.");
        }

        workoutRecordRepository.deleteAll(records);
    }
}
