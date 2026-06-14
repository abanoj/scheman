package com.abanoj.scheman.shiftassignment;

import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.config.RateLimitProperties;
import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.security.JwtService;
import com.abanoj.scheman.shiftassignment.controller.ShiftAssignmentController;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
import com.abanoj.scheman.shiftassignment.service.ShiftAssignmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShiftAssignmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShiftAssignmentControllerTest {

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

    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    private UUID shiftId;
    private UUID assignmentId;
    private UUID employeeId;
    private ShiftAssignmentResponseDto responseDto;

    @BeforeEach
    void setUp() {
        shiftId = UUID.randomUUID();
        assignmentId = UUID.randomUUID();
        employeeId = UUID.randomUUID();

        responseDto = new ShiftAssignmentResponseDto(
                assignmentId,
                LocalDate.of(2026, 6, 2),
                employeeId,
                shiftId,
                null
        );
    }

    private String baseUrl() {
        return "/api/v1/shifts/" + shiftId + "/shift-assignments";
    }

    @Nested
    @DisplayName("GET /api/v1/shifts/{shiftId}/shift-assignments")
    class GetAllByShiftId {

        @Test
        void shouldReturnPageOfAssignments_whenAssignmentsExist() throws Exception {
            //given
            Page<ShiftAssignmentResponseDto> page = new PageImpl<>(List.of(responseDto));
            given(shiftAssignmentService.findAllShiftsAssignmentsByShiftId(any(Pageable.class), eq(shiftId)))
                    .willReturn(page);

            //when & then
            mockMvc.perform(get(baseUrl()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(assignmentId.toString()))
                    .andExpect(jsonPath("$.content[0].date").value("2026-06-02"))
                    .andExpect(jsonPath("$.content[0].employeeId").value(employeeId.toString()))
                    .andExpect(jsonPath("$.content[0].shiftId").value(shiftId.toString()));
        }

        @Test
        void shouldReturnEmptyPage_whenNoAssignmentsExist() throws Exception {
            //given
            Page<ShiftAssignmentResponseDto> emptyPage = Page.empty();
            given(shiftAssignmentService.findAllShiftsAssignmentsByShiftId(any(Pageable.class), eq(shiftId)))
                    .willReturn(emptyPage);

            //when & then
            mockMvc.perform(get(baseUrl()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/shifts/{shiftId}/shift-assignments/employee/{employeeId}")
    class GetByEmployeeId {

        @Test
        void shouldReturnPageOfAssignments_whenAssignmentsExist() throws Exception {
            //given
            Page<ShiftAssignmentResponseDto> page = new PageImpl<>(List.of(responseDto));
            given(shiftAssignmentService.findAllShiftsAssignmentsByEmployeeId(any(Pageable.class), eq(employeeId)))
                    .willReturn(page);

            //when & then
            mockMvc.perform(get(baseUrl() + "/employee/{employeeId}", employeeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(assignmentId.toString()))
                    .andExpect(jsonPath("$.content[0].employeeId").value(employeeId.toString()));
        }

        @Test
        void shouldReturnEmptyPage_whenNoAssignmentsExist() throws Exception {
            //given
            Page<ShiftAssignmentResponseDto> emptyPage = Page.empty();
            given(shiftAssignmentService.findAllShiftsAssignmentsByEmployeeId(any(Pageable.class), eq(employeeId)))
                    .willReturn(emptyPage);

            //when & then
            mockMvc.perform(get(baseUrl() + "/employee/{employeeId}", employeeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/shifts/{shiftId}/shift-assignments/{id}")
    class GetById {

        @Test
        void shouldReturnAssignment_whenExists() throws Exception {
            //given
            given(shiftAssignmentService.findShiftAssignmentById(shiftId, assignmentId)).willReturn(responseDto);

            //when & then
            mockMvc.perform(get(baseUrl() + "/{id}", assignmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(assignmentId.toString()))
                    .andExpect(jsonPath("$.date").value("2026-06-02"))
                    .andExpect(jsonPath("$.employeeId").value(employeeId.toString()))
                    .andExpect(jsonPath("$.shiftId").value(shiftId.toString()));
        }

        @Test
        void shouldReturnNotFound_whenAssignmentDoesNotExist() throws Exception {
            //given
            given(shiftAssignmentService.findShiftAssignmentById(shiftId, assignmentId))
                    .willThrow(new ResourceNotFoundException("Shift assignment not found with id: " + assignmentId));

            //when & then
            mockMvc.perform(get(baseUrl() + "/{id}", assignmentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Shift assignment not found with id: " + assignmentId));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/shifts/{shiftId}/shift-assignments")
    class CreateShiftAssignment {

        private ShiftAssignmentCreateRequestDto createRequest;

        @BeforeEach
        void setUp() {
            createRequest = new ShiftAssignmentCreateRequestDto(
                    LocalDate.of(2026, 6, 2),
                    employeeId
            );
        }

        @Test
        void shouldCreateAssignment_whenRequestIsValid() throws Exception {
            //given
            given(shiftAssignmentService.createShiftAssignment(eq(shiftId), any(ShiftAssignmentCreateRequestDto.class)))
                    .willReturn(responseDto);

            //when & then
            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(assignmentId.toString()))
                    .andExpect(jsonPath("$.date").value("2026-06-02"))
                    .andExpect(jsonPath("$.employeeId").value(employeeId.toString()));
        }

        @Test
        void shouldReturnBadRequest_whenDateIsNull() throws Exception {
            //given
            ShiftAssignmentCreateRequestDto invalidRequest = new ShiftAssignmentCreateRequestDto(
                    null, employeeId
            );

            //when & then
            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnNotFound_whenShiftDoesNotExist() throws Exception {
            //given
            given(shiftAssignmentService.createShiftAssignment(eq(shiftId), any(ShiftAssignmentCreateRequestDto.class)))
                    .willThrow(new ResourceNotFoundException("Shift not found with id: " + shiftId));

            //when & then
            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Shift not found with id: " + shiftId));
        }

        @Test
        void shouldReturnConflict_whenAssignmentOverlaps() throws Exception {
            //given
            given(shiftAssignmentService.createShiftAssignment(eq(shiftId), any(ShiftAssignmentCreateRequestDto.class)))
                    .willThrow(new ConflictException("Assignment overlaps with an existing one"));

            //when & then
            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Assignment overlaps with an existing one"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/shifts/{shiftId}/shift-assignments/{id}")
    class UpdateShiftAssignment {

        private ShiftAssignmentUpdateRequestDto updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new ShiftAssignmentUpdateRequestDto(
                    LocalDate.of(2026, 6, 3),
                    employeeId
            );
        }

        @Test
        void shouldUpdateAssignment_whenRequestIsValid() throws Exception {
            //given
            ShiftAssignmentResponseDto updatedResponse = new ShiftAssignmentResponseDto(
                    assignmentId, LocalDate.of(2026, 6, 3), employeeId, shiftId, null
            );
            given(shiftAssignmentService.updateShiftAssignment(eq(shiftId), eq(assignmentId), any(ShiftAssignmentUpdateRequestDto.class)))
                    .willReturn(updatedResponse);

            //when & then
            mockMvc.perform(put(baseUrl() + "/{id}", assignmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(assignmentId.toString()))
                    .andExpect(jsonPath("$.date").value("2026-06-03"));
        }

        @Test
        void shouldReturnBadRequest_whenEmployeeIdIsNull() throws Exception {
            //given
            ShiftAssignmentUpdateRequestDto invalidRequest = new ShiftAssignmentUpdateRequestDto(
                    LocalDate.of(2026, 6, 3), null
            );

            //when & then
            mockMvc.perform(put(baseUrl() + "/{id}", assignmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnNotFound_whenAssignmentDoesNotExist() throws Exception {
            //given
            given(shiftAssignmentService.updateShiftAssignment(eq(shiftId), eq(assignmentId), any(ShiftAssignmentUpdateRequestDto.class)))
                    .willThrow(new ResourceNotFoundException("Shift assignment not found with id: " + assignmentId));

            //when & then
            mockMvc.perform(put(baseUrl() + "/{id}", assignmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnConflict_whenAssignmentOverlaps() throws Exception {
            //given
            given(shiftAssignmentService.updateShiftAssignment(eq(shiftId), eq(assignmentId), any(ShiftAssignmentUpdateRequestDto.class)))
                    .willThrow(new ConflictException("Assignment overlaps with an existing one"));

            //when & then
            mockMvc.perform(put(baseUrl() + "/{id}", assignmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Assignment overlaps with an existing one"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/shifts/{shiftId}/shift-assignments/{id}")
    class DeleteShiftAssignment {

        @Test
        void shouldReturnNoContent_whenAssignmentIsDeleted() throws Exception {
            //given
            willDoNothing().given(shiftAssignmentService).delete(shiftId, assignmentId);

            //when & then
            mockMvc.perform(delete(baseUrl() + "/{id}", assignmentId))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturnNotFound_whenAssignmentDoesNotExist() throws Exception {
            //given
            willThrow(new ResourceNotFoundException("Shift assignment not found with id: " + assignmentId))
                    .given(shiftAssignmentService).delete(shiftId, assignmentId);

            //when & then
            mockMvc.perform(delete(baseUrl() + "/{id}", assignmentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Shift assignment not found with id: " + assignmentId));
        }
    }
}
