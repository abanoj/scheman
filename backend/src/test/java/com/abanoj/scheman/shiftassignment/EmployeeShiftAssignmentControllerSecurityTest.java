package com.abanoj.scheman.shiftassignment;

import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.config.RateLimitProperties;
import com.abanoj.scheman.security.JwtService;
import com.abanoj.scheman.shiftassignment.controller.EmployeeShiftAssignmentController;
import com.abanoj.scheman.shiftassignment.service.ShiftAssignmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeShiftAssignmentController.class)
@Import(EmployeeShiftAssignmentControllerSecurityTest.TestMethodSecurityConfig.class)
@DisplayName("EmployeeShiftAssignmentController - Security")
public class EmployeeShiftAssignmentControllerSecurityTest {

    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShiftAssignmentService shiftAssignmentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private CorsProperties corsProperties;

    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    private static final String BASE_URL = "/api/v1/shift-assignments/employees";
    private static final UUID EMPLOYEE_ID = UUID.randomUUID();

    @Nested
    @DisplayName("Unauthenticated requests")
    class Unauthenticated {

        @Test
        void shouldReturn401_whenAccessingWeeklyWithoutAuth() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{employeeId}/weekly", EMPLOYEE_ID)
                            .param("date", "2026-06-01"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
