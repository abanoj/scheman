package com.abanoj.scheman.employee;

import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.employee.controller.EmployeeController;
import com.abanoj.scheman.employee.dto.EmployeeCreateRequestDto;
import com.abanoj.scheman.employee.service.EmployeeService;
import com.abanoj.scheman.security.JwtService;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
@Import(EmployeeControllerSecurityTest.TestMethodSecurityConfig.class)
@DisplayName("EmployeeController - Security")
public class EmployeeControllerSecurityTest {

    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private CorsProperties corsProperties;

    private static final String BASE_URL = "/api/v1/employees";
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Unauthenticated requests")
    class Unauthenticated {

        @Test
        void shouldReturn401_whenAccessingGetAllWithoutAuth() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingCreateWithoutAuth() throws Exception {
            EmployeeCreateRequestDto request = new EmployeeCreateRequestDto(
                    "Y8479910J", "John", "Doe", "john@doe.com", ShiftType.MORNING, List.of(), 40
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingGetByIdWithoutAuth() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{employeeId}", EMPLOYEE_ID))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingMeWithoutAuth() throws Exception {
            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingUpdateWithoutAuth() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{employeeId}", EMPLOYEE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingDisableWithoutAuth() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{employeeId}/disable", EMPLOYEE_ID)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingEnableWithoutAuth() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{employeeId}/enable", EMPLOYEE_ID)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Forbidden for EMPLOYEE role")
    @WithMockUser(roles = "EMPLOYEE")
    class ForbiddenForEmployee {

        @Test
        void shouldReturn403_whenEmployeeAccessesGetAll() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesCreate() throws Exception {
            EmployeeCreateRequestDto request = new EmployeeCreateRequestDto(
                    "Y8479910J", "John", "Doe", "john@doe.com", ShiftType.MORNING, List.of(), 40
            );

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesGetById() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{employeeId}", EMPLOYEE_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesUpdate() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{employeeId}", EMPLOYEE_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesDisable() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{employeeId}/disable", EMPLOYEE_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesEnable() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{employeeId}/enable", EMPLOYEE_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
