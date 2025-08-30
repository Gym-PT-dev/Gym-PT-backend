package com.Gym_PT.controller;

import com.Gym_PT.entity.Users;
import com.Gym_PT.service.UsersService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsersController.class)
@ActiveProfiles("test")
class UsersControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean UsersService usersService;

    @Test
    void createUser_returns_200_and_body() throws Exception {
        Users req = Users.builder().googleSub("sub-new").email("n@test.com").name("N").build();
        Users res = Users.builder().id(10L).googleSub("sub-new").email("n@test.com").name("N").build();

        given(usersService.createOrGetByGoogleSub(ArgumentMatchers.any(Users.class))).willReturn(res);

        String body = """
          {"googleSub":"sub-new","email":"n@test.com","name":"N"}
        """;

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.googleSub").value("sub-new"));
    }

    @Test
    void getAllUsers_returns_list() throws Exception {
        Users u1 = Users.builder().id(1L).googleSub("s1").name("A").build();
        Users u2 = Users.builder().id(2L).googleSub("s2").name("B").build();
        given(usersService.getAll()).willReturn(List.of(u1, u2));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void getUser_found_200() throws Exception {
        Users u = Users.builder().id(3L).googleSub("s3").name("C").build();
        given(usersService.getUser(3L)).willReturn(Optional.of(u));

        mockMvc.perform(get("/users/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void getUser_not_found_404() throws Exception {
        given(usersService.getUser(99L)).willReturn(Optional.empty());

        mockMvc.perform(get("/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_204_and_calls_service() throws Exception {
        mockMvc.perform(delete("/users/7"))
                .andExpect(status().isNoContent());
        verify(usersService).deleteUser(7L);
    }
}
