package com.Gym_PT.controller;

import com.Gym_PT.entity.UserProfile;
import com.Gym_PT.entity.Users;
import com.Gym_PT.service.UserProfileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
@ActiveProfiles("test")
class UserProfileControllerTest {

    @Autowired MockMvc mvc;
    @MockBean UserProfileService profileService;

    @Test
    void getProfile_ok_200_and_body() throws Exception {
        Users user = Users.builder().id(1L).googleSub("sub").build();
        UserProfile profile = UserProfile.builder()
                .id(10L).user(user).age(30)
                .gender(UserProfile.Gender.MALE)
                .height(180f).weight(80f)
                .goal(UserProfile.Goal.BULK)
                .experienceLevel(UserProfile.ExperienceLevel.INTERMEDIATE)
                .duration(240).location("Gym").splitStrategy("PPL")
                .build();

        given(profileService.getByUserId(1L)).willReturn(Optional.of(profile));

        mvc.perform(get("/users/1/profile"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.age").value(30))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.goal").value("BULK"));
    }

    @Test
    void getProfile_not_found_404() throws Exception {
        given(profileService.getByUserId(99L)).willReturn(Optional.empty());
        mvc.perform(get("/users/99/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    void upsertProfile_ok_200_and_returns_updated() throws Exception {
        Users user = Users.builder().id(2L).googleSub("sub2").build();
        UserProfile saved = UserProfile.builder()
                .id(20L).user(user).age(27)
                .gender(UserProfile.Gender.FEMALE)
                .height(165f).weight(54f)
                .goal(UserProfile.Goal.KEEP)
                .experienceLevel(UserProfile.ExperienceLevel.BEGINNER)
                .duration(150).location("Home").splitStrategy("FBW")
                .build();

        given(profileService.upsert(any(Long.class), any(UserProfile.class))).willReturn(saved);

        String body = """
        {
          "age":27,
          "gender":"FEMALE",
          "height":165.0,
          "weight":54.0,
          "goal":"KEEP",
          "experienceLevel":"BEGINNER",
          "duration":150,
          "location":"Home",
          "splitStrategy":"FBW"
        }
        """;

        mvc.perform(put("/users/2/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(20))
                .andExpect(jsonPath("$.gender").value("FEMALE"))
                .andExpect(jsonPath("$.goal").value("KEEP"));
    }
}
