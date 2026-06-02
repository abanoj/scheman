package com.abanoj.scheman.shift;

import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.config.RateLimitProperties;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.security.JwtService;
import com.abanoj.scheman.shift.controller.ShiftController;
import com.abanoj.scheman.shift.dto.ShiftCreateRequestDto;
import com.abanoj.scheman.shift.dto.ShiftResponseDto;
import com.abanoj.scheman.shift.dto.ShiftUpdateRequestDto;
import com.abanoj.scheman.shift.dto.UnassignedShiftResponseDto;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.abanoj.scheman.shift.service.ShiftService;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShiftController.class)
@AutoConfigureMockMvc(addFilters = false)
class ShiftControllerTest {

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

    private UUID storeId;
    private UUID shiftId;
    private ShiftResponseDto shiftResponseDto;

    @BeforeEach
    void setUp() {
        storeId = UUID.randomUUID();
        shiftId = UUID.randomUUID();

        shiftResponseDto = new ShiftResponseDto(
                shiftId,
                "Morning",
                storeId,
                LocalTime.of(8, 0),
                LocalTime.of(16, 0),
                ShiftType.MORNING,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                false
        );
    }

    private String baseUrl() {
        return "/api/v1/stores/" + storeId + "/shifts";
    }

    @Nested
    @DisplayName("GET /api/v1/stores/{storeId}/shifts")
    class GetAllShiftsByStoreId {

        @Test
        void shouldReturnPageOfShifts_whenShiftsExist() throws Exception {
            //given
            Page<ShiftResponseDto> page = new PageImpl<>(List.of(shiftResponseDto));
            given(shiftService.findAllShiftsByStoreId(any(Pageable.class), eq(storeId))).willReturn(page);

            //when & then
            mockMvc.perform(get(baseUrl()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(shiftId.toString()))
                    .andExpect(jsonPath("$.content[0].name").value("Morning"))
                    .andExpect(jsonPath("$.content[0].storeId").value(storeId.toString()))
                    .andExpect(jsonPath("$.content[0].shiftType").value("MORNING"))
                    .andExpect(jsonPath("$.content[0].crossesMidnight").value(false));
        }

        @Test
        void shouldReturnEmptyPage_whenNoShiftsExist() throws Exception {
            //given
            Page<ShiftResponseDto> emptyPage = Page.empty();
            given(shiftService.findAllShiftsByStoreId(any(Pageable.class), eq(storeId))).willReturn(emptyPage);

            //when & then
            mockMvc.perform(get(baseUrl()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/{storeId}/shifts/unassigned")
    class GetUnassignedShifts {

        @Test
        void shouldReturnUnassignedShifts_whenTheyExist() throws Exception {
            //given
            UUID unassignedShiftId = UUID.randomUUID();
            UnassignedShiftResponseDto unassigned = new UnassignedShiftResponseDto(
                    unassignedShiftId, "Morning", Set.of(DayOfWeek.TUESDAY)
            );
            LocalDate date = LocalDate.of(2026, 6, 1);
            given(shiftService.findUnassignedShifts(eq(storeId), eq(date))).willReturn(List.of(unassigned));

            //when & then
            mockMvc.perform(get(baseUrl() + "/unassigned").param("date", "2026-06-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].shiftId").value(unassignedShiftId.toString()))
                    .andExpect(jsonPath("$[0].shiftName").value("Morning"));
        }

        @Test
        void shouldReturnEmptyList_whenNoUnassignedShiftsExist() throws Exception {
            //given
            LocalDate date = LocalDate.of(2026, 6, 1);
            given(shiftService.findUnassignedShifts(eq(storeId), eq(date))).willReturn(List.of());

            //when & then
            mockMvc.perform(get(baseUrl() + "/unassigned").param("date", "2026-06-01"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/{storeId}/shifts/{id}")
    class GetShiftById {

        @Test
        void shouldReturnShift_whenExists() throws Exception {
            //given
            given(shiftService.findShiftById(shiftId, storeId)).willReturn(shiftResponseDto);

            //when & then
            mockMvc.perform(get(baseUrl() + "/{id}", shiftId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(shiftId.toString()))
                    .andExpect(jsonPath("$.name").value("Morning"))
                    .andExpect(jsonPath("$.storeId").value(storeId.toString()));
        }

        @Test
        void shouldReturnNotFound_whenShiftDoesNotExist() throws Exception {
            //given
            given(shiftService.findShiftById(shiftId, storeId))
                    .willThrow(new ResourceNotFoundException("Shift not found with id: " + shiftId));

            //when & then
            mockMvc.perform(get(baseUrl() + "/{id}", shiftId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Shift not found with id: " + shiftId));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/stores/{storeId}/shifts")
    class CreateShift {

        private ShiftCreateRequestDto createRequest;

        @BeforeEach
        void setUp() {
            createRequest = new ShiftCreateRequestDto(
                    "Morning",
                    LocalTime.of(8, 0),
                    LocalTime.of(16, 0),
                    LocalDate.of(2026, 6, 1),
                    ShiftType.MORNING,
                    Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
            );
        }

        @Test
        void shouldCreateShift_whenRequestIsValid() throws Exception {
            //given
            given(shiftService.createShift(eq(storeId), any(ShiftCreateRequestDto.class))).willReturn(shiftResponseDto);

            //when & then
            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(shiftId.toString()))
                    .andExpect(jsonPath("$.name").value("Morning"))
                    .andExpect(jsonPath("$.shiftType").value("MORNING"));
        }

        @Test
        void shouldReturnBadRequest_whenNameIsBlank() throws Exception {
            //given
            ShiftCreateRequestDto invalidRequest = new ShiftCreateRequestDto(
                    "", LocalTime.of(8, 0), LocalTime.of(16, 0),
                    LocalDate.of(2026, 6, 1), ShiftType.MORNING,
                    Set.of(DayOfWeek.MONDAY)
            );

            //when & then
            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnNotFound_whenStoreDoesNotExist() throws Exception {
            //given
            given(shiftService.createShift(eq(storeId), any(ShiftCreateRequestDto.class)))
                    .willThrow(new ResourceNotFoundException("Store not found with id: " + storeId));

            //when & then
            mockMvc.perform(post(baseUrl())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Store not found with id: " + storeId));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/stores/{storeId}/shifts/{id}")
    class UpdateShift {

        private ShiftUpdateRequestDto updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new ShiftUpdateRequestDto(
                    "Afternoon",
                    LocalTime.of(14, 0),
                    LocalTime.of(22, 0),
                    ShiftType.AFTERNOON,
                    Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
            );
        }

        @Test
        void shouldUpdateShift_whenRequestIsValid() throws Exception {
            //given
            ShiftResponseDto updatedResponse = new ShiftResponseDto(
                    shiftId, "Afternoon", storeId,
                    LocalTime.of(14, 0), LocalTime.of(22, 0),
                    ShiftType.AFTERNOON,
                    Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
                    false
            );
            given(shiftService.updateShift(eq(storeId), eq(shiftId), any(ShiftUpdateRequestDto.class)))
                    .willReturn(updatedResponse);

            //when & then
            mockMvc.perform(put(baseUrl() + "/{id}", shiftId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Afternoon"))
                    .andExpect(jsonPath("$.shiftType").value("AFTERNOON"));
        }

        @Test
        void shouldReturnBadRequest_whenNameIsBlank() throws Exception {
            //given
            ShiftUpdateRequestDto invalidRequest = new ShiftUpdateRequestDto(
                    "", LocalTime.of(14, 0), LocalTime.of(22, 0),
                    ShiftType.AFTERNOON, Set.of(DayOfWeek.MONDAY)
            );

            //when & then
            mockMvc.perform(put(baseUrl() + "/{id}", shiftId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnNotFound_whenShiftDoesNotExist() throws Exception {
            //given
            given(shiftService.updateShift(eq(storeId), eq(shiftId), any(ShiftUpdateRequestDto.class)))
                    .willThrow(new ResourceNotFoundException("Shift not found with id: " + shiftId));

            //when & then
            mockMvc.perform(put(baseUrl() + "/{id}", shiftId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/stores/{storeId}/shifts/{id}")
    class DeleteShift {

        @Test
        void shouldReturnNoContent_whenShiftIsDeleted() throws Exception {
            //given
            willDoNothing().given(shiftService).deleteShift(storeId, shiftId);

            //when & then
            mockMvc.perform(delete(baseUrl() + "/{id}", shiftId))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturnNotFound_whenShiftDoesNotExist() throws Exception {
            //given
            willThrow(new ResourceNotFoundException("Shift not found with id: " + shiftId))
                    .given(shiftService).deleteShift(storeId, shiftId);

            //when & then
            mockMvc.perform(delete(baseUrl() + "/{id}", shiftId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Shift not found with id: " + shiftId));
        }
    }
}
