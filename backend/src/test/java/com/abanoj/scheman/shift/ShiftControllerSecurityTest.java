package com.abanoj.scheman.shift;

import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.config.RateLimitProperties;
import com.abanoj.scheman.security.JwtService;
import com.abanoj.scheman.shift.controller.ShiftController;
import com.abanoj.scheman.shift.dto.ShiftCreateRequestDto;
import com.abanoj.scheman.shift.dto.ShiftUpdateRequestDto;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.abanoj.scheman.shift.service.ShiftService;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShiftController.class)
@Import(ShiftControllerSecurityTest.TestMethodSecurityConfig.class)
@DisplayName("ShiftController - Security")
public class ShiftControllerSecurityTest {

    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShiftService shiftService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private CorsProperties corsProperties;

    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    private static final UUID STORE_ID = UUID.randomUUID();
    private static final UUID SHIFT_ID = UUID.randomUUID();

    private String baseUrl() {
        return "/api/v1/stores/" + STORE_ID + "/shifts";
    }

    @Nested
    @DisplayName("Unauthenticated requests")
    class Unauthenticated {

        @Test
        void shouldReturn401_whenAccessingGetAllWithoutAuth() throws Exception {
            mockMvc.perform(get(baseUrl()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingGetUnassignedWithoutAuth() throws Exception {
            mockMvc.perform(get(baseUrl() + "/unassigned").param("date", "2026-06-01"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingGetByIdWithoutAuth() throws Exception {
            mockMvc.perform(get(baseUrl() + "/{id}", SHIFT_ID))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingCreateWithoutAuth() throws Exception {
            ShiftCreateRequestDto request = new ShiftCreateRequestDto(
                    "Morning", LocalTime.of(8, 0), LocalTime.of(16, 0),
                    LocalDate.of(2026, 6, 1), ShiftType.MORNING,
                    Set.of(DayOfWeek.MONDAY)
            );

            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingUpdateWithoutAuth() throws Exception {
            ShiftUpdateRequestDto request = new ShiftUpdateRequestDto(
                    "Morning", LocalTime.of(8, 0), LocalTime.of(16, 0),
                    ShiftType.MORNING, Set.of(DayOfWeek.MONDAY)
            );

            mockMvc.perform(put(baseUrl() + "/{id}", SHIFT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingDeleteWithoutAuth() throws Exception {
            mockMvc.perform(delete(baseUrl() + "/{id}", SHIFT_ID)
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
            mockMvc.perform(get(baseUrl()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesGetUnassigned() throws Exception {
            mockMvc.perform(get(baseUrl() + "/unassigned").param("date", "2026-06-01"))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesGetById() throws Exception {
            mockMvc.perform(get(baseUrl() + "/{id}", SHIFT_ID))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesCreate() throws Exception {
            ShiftCreateRequestDto request = new ShiftCreateRequestDto(
                    "Morning", LocalTime.of(8, 0), LocalTime.of(16, 0),
                    LocalDate.of(2026, 6, 1), ShiftType.MORNING,
                    Set.of(DayOfWeek.MONDAY)
            );

            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesUpdate() throws Exception {
            ShiftUpdateRequestDto request = new ShiftUpdateRequestDto(
                    "Morning", LocalTime.of(8, 0), LocalTime.of(16, 0),
                    ShiftType.MORNING, Set.of(DayOfWeek.MONDAY)
            );

            mockMvc.perform(put(baseUrl() + "/{id}", SHIFT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenEmployeeAccessesDelete() throws Exception {
            mockMvc.perform(delete(baseUrl() + "/{id}", SHIFT_ID)
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }
}
