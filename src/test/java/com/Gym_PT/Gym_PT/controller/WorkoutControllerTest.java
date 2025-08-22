package com.Gym_PT.Gym_PT.controller;

import com.Gym_PT.Gym_PT.dto.WorkoutRequestDto;
import com.Gym_PT.Gym_PT.dto.WorkoutResponseDto;
import com.Gym_PT.Gym_PT.service.WorkoutService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkoutController.class)
@ActiveProfiles("test")
class WorkoutControllerTest {

    @Autowired MockMvc mvc;

    @MockBean WorkoutService workoutService;

    @Test
    void post_saveWorkout_returns_200_and_body() throws Exception {
        String body = """
        {
          "date":"2025-08-05",
          "running":[{"runningName":"런닝","speed":9.5,"duration":20}],
          "nonWeight":[{"nonWeightName":"푸쉬업","nonWeightSets":3,"nonWeightReps":15}],
          "weight":[{"weightName":"벤치프레스","weightSets":4,"weightReps":8,"weight":60}]
        }
        """;

        // 서비스는 그대로 echo 한다고 가정
        WorkoutRequestDto echo = new WorkoutRequestDto();
        echo.setDate(LocalDate.parse("2025-08-05"));
        var r = new WorkoutRequestDto.RunningDto(); r.setRunningName("런닝"); r.setSpeed(9.5f); r.setDuration(20);
        echo.setRunning(List.of(r));
        given(workoutService.saveWorkout(Mockito.eq(1L), any(WorkoutRequestDto.class))).willReturn(echo);

        mvc.perform(post("/users/1/workout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.date").value("2025-08-05"))
                .andExpect(jsonPath("$.running[0].runningName").value("런닝"));

        // 전달된 DTO가 파싱되었는지 캡처로 확인(옵션)
        ArgumentCaptor<WorkoutRequestDto> captor = ArgumentCaptor.forClass(WorkoutRequestDto.class);
        verify(workoutService).saveWorkout(Mockito.eq(1L), captor.capture());
        assertThat(captor.getValue().getDate()).isEqualTo(LocalDate.parse("2025-08-05"));
    }

    @Test
    void get_getWorkoutByDate_returns_200() throws Exception {
        WorkoutResponseDto res = new WorkoutResponseDto();
        res.setUserId(2L);
        res.setDate(LocalDate.parse("2025-08-06"));
        given(workoutService.getWorkoutByUserAndDate(2L, LocalDate.parse("2025-08-06"))).willReturn(res);

        mvc.perform(get("/users/2/workout").param("date", "2025-08-06"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.date").value("2025-08-06"));
    }

    @Test
    void delete_deleteWorkoutByDate_returns_204() throws Exception {
        mvc.perform(delete("/users/3/workout").param("date","2025-08-07"))
                .andExpect(status().isNoContent());

        verify(workoutService).deleteWorkoutByUserAndDate(3L, LocalDate.parse("2025-08-07"));
    }
}
