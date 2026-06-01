package com.abanoj.scheman.shiftassignment;

import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.security.JwtService;
import com.abanoj.scheman.shiftassignment.controller.EmployeeShiftAssignmentController;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.service.ShiftAssignmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeShiftAssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeShiftAssignmentControllerTest {

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

    private static final String BASE_URL = "/api/v1/shift-assignments/employees";

    private UUID employeeId;
    private UUID shiftId;
    private UUID assignmentId;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        shiftId = UUID.randomUUID();
        assignmentId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("GET /api/v1/shift-assignments/employees/{employeeId}/weekly")
    class GetWeeklyByEmployee {

        @Test
        void shouldReturnWeeklyAssignments_whenTheyExist() throws Exception {
            //given
            LocalDate date = LocalDate.of(2026, 6, 1);
            ShiftAssignmentResponseDto responseDto = new ShiftAssignmentResponseDto(
                    assignmentId, LocalDate.of(2026, 6, 2), employeeId, shiftId
            );
            given(shiftAssignmentService.findWeeklyAssignmentsByEmployee(eq(employeeId), eq(date)))
                    .willReturn(List.of(responseDto));

            //when & then
            mockMvc.perform(get(BASE_URL + "/{employeeId}/weekly", employeeId)
                            .param("date", "2026-06-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(assignmentId.toString()))
                    .andExpect(jsonPath("$[0].date").value("2026-06-02"))
                    .andExpect(jsonPath("$[0].employeeId").value(employeeId.toString()))
                    .andExpect(jsonPath("$[0].shiftId").value(shiftId.toString()));
        }

        @Test
        void shouldReturnNotFound_whenEmployeeDoesNotExist() throws Exception {
            //given
            LocalDate date = LocalDate.of(2026, 6, 1);
            given(shiftAssignmentService.findWeeklyAssignmentsByEmployee(eq(employeeId), eq(date)))
                    .willThrow(new ResourceNotFoundException("Employee not found with id: " + employeeId));

            //when & then
            mockMvc.perform(get(BASE_URL + "/{employeeId}/weekly", employeeId)
                            .param("date", "2026-06-01"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Employee not found with id: " + employeeId));
        }
    }
}
