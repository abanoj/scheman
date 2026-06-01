package com.abanoj.scheman.shiftassignment;

import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.security.JwtService;
import com.abanoj.scheman.shiftassignment.controller.ShiftAssignmentController;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
import com.abanoj.scheman.shiftassignment.service.ShiftAssignmentService;
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

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShiftAssignmentController.class)
@Import(ShiftAssignmentControllerSecurityTest.TestMethodSecurityConfig.class)
@DisplayName("ShiftAssignmentController - Security")
public class ShiftAssignmentControllerSecurityTest {

    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShiftAssignmentService shiftAssignmentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private CorsProperties corsProperties;

    private static final UUID SHIFT_ID = UUID.randomUUID();
    private static final UUID ASSIGNMENT_ID = UUID.randomUUID();
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();

    private String baseUrl() {
        return "/api/v1/shifts/" + SHIFT_ID + "/shift-assignments";
    }

    @Nested
    @DisplayName("Unauthenticated requests")
    class Unauthenticated {

        @Test
        void shouldReturn401_whenAccessingGetAllByShiftIdWithoutAuth() throws Exception {
            mockMvc.perform(get(baseUrl()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingGetByEmployeeIdWithoutAuth() throws Exception {
            mockMvc.perform(get(baseUrl() + "/employee/{employeeId}", EMPLOYEE_ID))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingGetByIdWithoutAuth() throws Exception {
            mockMvc.perform(get(baseUrl() + "/{id}", ASSIGNMENT_ID))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingCreateWithoutAuth() throws Exception {
            ShiftAssignmentCreateRequestDto request = new ShiftAssignmentCreateRequestDto(
                    LocalDate.of(2026, 6, 2), EMPLOYEE_ID
            );

            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingUpdateWithoutAuth() throws Exception {
            ShiftAssignmentUpdateRequestDto request = new ShiftAssignmentUpdateRequestDto(
                    LocalDate.of(2026, 6, 2), EMPLOYEE_ID
            );

            mockMvc.perform(put(baseUrl() + "/{id}", ASSIGNMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingDeleteWithoutAuth() throws Exception {
            mockMvc.perform(delete(baseUrl() + "/{id}", ASSIGNMENT_ID)
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Forbidden for EMPLOYEE role")
    @WithMockUser(roles = "EMPLOYEE")
    class ForbiddenForEmployee {

        @Test
        void shouldReturn403_whenEmployeeAccessesGetAllByShiftId() throws Exception {
            mockMvc.perform(get(baseUrl()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesGetById() throws Exception {
            mockMvc.perform(get(baseUrl() + "/{id}", ASSIGNMENT_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesCreate() throws Exception {
            ShiftAssignmentCreateRequestDto request = new ShiftAssignmentCreateRequestDto(
                    LocalDate.of(2026, 6, 2), EMPLOYEE_ID
            );

            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesUpdate() throws Exception {
            ShiftAssignmentUpdateRequestDto request = new ShiftAssignmentUpdateRequestDto(
                    LocalDate.of(2026, 6, 2), EMPLOYEE_ID
            );

            mockMvc.perform(put(baseUrl() + "/{id}", ASSIGNMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesDelete() throws Exception {
            mockMvc.perform(delete(baseUrl() + "/{id}", ASSIGNMENT_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
