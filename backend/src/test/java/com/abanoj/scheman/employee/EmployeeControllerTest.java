package com.abanoj.scheman.employee;

import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.employee.controller.EmployeeController;
import com.abanoj.scheman.employee.dto.EmployeeCreateRequestDto;
import com.abanoj.scheman.employee.dto.EmployeeResponseDto;
import com.abanoj.scheman.employee.dto.EmployeeUpdateRequestDto;
import com.abanoj.scheman.employee.service.EmployeeService;
import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.security.JwtService;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.abanoj.scheman.store.dto.StoreSummaryResponseDto;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

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

    private UUID employeeId;
    private UUID userId;
    private UUID storeId;
    private EmployeeResponseDto employeeResponseDto;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        userId = UUID.randomUUID();
        storeId = UUID.randomUUID();

        StoreSummaryResponseDto storeSummary = new StoreSummaryResponseDto(storeId, "San Blas");

        employeeResponseDto = new EmployeeResponseDto(
                employeeId,
                "Y8479910J",
                "John",
                "Doe",
                "john@doe.com",
                userId,
                ShiftType.MORNING,
                List.of(storeSummary),
                40
        );
    }

    @Nested
    @DisplayName("GET /api/v1/employees")
    class FindAllEmployees {

        @Test
        void shouldReturnPageOfEmployees_whenEmployeesExist() throws Exception {
            //given
            Page<EmployeeResponseDto> page = new PageImpl<>(List.of(employeeResponseDto));
            given(employeeService.findAllEmployees(any(Pageable.class))).willReturn(page);

            //when & then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(employeeId.toString()))
                    .andExpect(jsonPath("$.content[0].dni").value("Y8479910J"))
                    .andExpect(jsonPath("$.content[0].firstName").value("John"))
                    .andExpect(jsonPath("$.content[0].lastName").value("Doe"))
                    .andExpect(jsonPath("$.content[0].email").value("john@doe.com"))
                    .andExpect(jsonPath("$.content[0].preferredShift").value("MORNING"))
                    .andExpect(jsonPath("$.content[0].weeklyContractedHours").value(40));
        }


        @Test
        void shouldReturnEmptyPage_whenNoEmployeesExist() throws Exception {
            //given
            Page<EmployeeResponseDto> emptyPage = Page.empty();
            given(employeeService.findAllEmployees(any(Pageable.class))).willReturn(emptyPage);

            //when & then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/employees")
    class CreateEmployee {

        private EmployeeCreateRequestDto employeeCreateRequestDto;

        @BeforeEach
        void setUp() {
            employeeCreateRequestDto = new EmployeeCreateRequestDto(
                    "Y8479910J",
                    "John",
                    "Doe",
                    "john@doe.com",
                    ShiftType.MORNING,
                    List.of(storeId),
                    40
            );
        }

        @Test
        void shouldCreateEmployee_whenRequestIsValid() throws Exception {
            //given
            given(employeeService.create(any(EmployeeCreateRequestDto.class))).willReturn(employeeResponseDto);

            //when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeCreateRequestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(employeeId.toString()))
                    .andExpect(jsonPath("$.dni").value("Y8479910J"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.lastName").value("Doe"))
                    .andExpect(jsonPath("$.email").value("john@doe.com"))
                    .andExpect(jsonPath("$.preferredShift").value("MORNING"))
                    .andExpect(jsonPath("$.weeklyContractedHours").value(40));
        }

        @Test
        void shouldReturnConflict_whenEmailAlreadyExists() throws Exception {
            //given
            given(employeeService.create(any(EmployeeCreateRequestDto.class)))
                    .willThrow(new ConflictException("Email already in use"));

            //when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(employeeCreateRequestDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Email already in use"));
        }

        @Test
        void shouldReturnBadRequest_whenDniIsBlank() throws Exception {
            //given
            EmployeeCreateRequestDto invalidRequest = new EmployeeCreateRequestDto(
                    "", "John", "Doe", "john@doe.com", ShiftType.MORNING, null, 40
            );

            //when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
            //given
            EmployeeCreateRequestDto invalidRequest = new EmployeeCreateRequestDto(
                    "Y8479910J", "John", "Doe", "invalid-email", ShiftType.MORNING, null, 40
            );

            //when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenPreferredShiftIsNull() throws Exception {
            //given
            EmployeeCreateRequestDto invalidRequest = new EmployeeCreateRequestDto(
                    "Y8479910J", "John", "Doe", "john@doe.com", null, null, 40
            );

            //when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenWeeklyHoursIsNegative() throws Exception {
            //given
            EmployeeCreateRequestDto invalidRequest = new EmployeeCreateRequestDto(
                    "Y8479910J", "John", "Doe", "john@doe.com", ShiftType.MORNING, null, -5
            );

            //when & then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees/{employeeId}")
    class GetEmployeeById {

        @Test
        void shouldReturnEmployee_whenExists() throws Exception {
            //given
            given(employeeService.findEmployeeById(employeeId)).willReturn(employeeResponseDto);

            //when & then
            mockMvc.perform(get(BASE_URL + "/{employeeId}", employeeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(employeeId.toString()))
                    .andExpect(jsonPath("$.dni").value("Y8479910J"))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.email").value("john@doe.com"));
        }

        @Test
        void shouldReturnNotFound_whenEmployeeDoesNotExist() throws Exception {
            //given
            given(employeeService.findEmployeeById(employeeId))
                    .willThrow(new ResourceNotFoundException("Employee not found with id: " + employeeId));

            //when & then
            mockMvc.perform(get(BASE_URL + "/{employeeId}", employeeId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Employee not found with id: " + employeeId));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/employees/me")
    class GetMyProfile {

        @Test
        void shouldReturnEmployeeProfile_whenAuthenticated() throws Exception {
            //given
            User user = User.builder()
                    .id(employeeId)
                    .firstName("John")
                    .lastName("Doe")
                    .email("john@doe.com")
                    .password("encoded")
                    .role(Role.EMPLOYEE)
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            given(employeeService.findEmployeeById(employeeId)).willReturn(employeeResponseDto);

            //when & then
            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(employeeId.toString()))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.email").value("john@doe.com"));
        }

        @Test
        void shouldReturnNotFound_whenEmployeeDoesNotExist() throws Exception {
            //given
            UUID userId = UUID.randomUUID();
            User user = User.builder()
                    .id(userId)
                    .firstName("Ghost")
                    .lastName("User")
                    .email("ghost@test.com")
                    .password("encoded")
                    .role(Role.EMPLOYEE)
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            given(employeeService.findEmployeeById(userId))
                    .willThrow(new ResourceNotFoundException("Employee not found with id: " + userId));

            //when & then
            mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/employees/{employeeId}")
    class UpdateEmployee {

        private EmployeeUpdateRequestDto updateRequest;

        @BeforeEach
        void setUp() {
            updateRequest = new EmployeeUpdateRequestDto(
                    null, "Jane", null, null, ShiftType.AFTERNOON, null, null
            );
        }

        @Test
        void shouldUpdateEmployee_whenRequestIsValid() throws Exception {
            //given
            EmployeeResponseDto updatedResponse = new EmployeeResponseDto(
                    employeeId, "Y8479910J", "Jane", "Doe", "john@doe.com",
                    userId, ShiftType.AFTERNOON, List.of(), 40
            );
            given(employeeService.updateEmployee(eq(employeeId), any(EmployeeUpdateRequestDto.class)))
                    .willReturn(updatedResponse);

            //when & then
            mockMvc.perform(patch(BASE_URL + "/{employeeId}", employeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Jane"))
                    .andExpect(jsonPath("$.preferredShift").value("AFTERNOON"));
        }

        @Test
        void shouldReturnNotFound_whenEmployeeDoesNotExist() throws Exception {
            //given
            given(employeeService.updateEmployee(eq(employeeId), any(EmployeeUpdateRequestDto.class)))
                    .willThrow(new ResourceNotFoundException("Employee not found with id: " + employeeId));

            //when & then
            mockMvc.perform(patch(BASE_URL + "/{employeeId}", employeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnBadRequest_whenDniHasInvalidLength() throws Exception {
            //given
            EmployeeUpdateRequestDto invalidRequest = new EmployeeUpdateRequestDto(
                    "ABC", null, null, null, null, null, null
            );

            //when & then
            mockMvc.perform(patch(BASE_URL + "/{employeeId}", employeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnBadRequest_whenEmailIsInvalid() throws Exception {
            //given
            EmployeeUpdateRequestDto invalidRequest = new EmployeeUpdateRequestDto(
                    null, null, null, "not-an-email", null, null, null
            );

            //when & then
            mockMvc.perform(patch(BASE_URL + "/{employeeId}", employeeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/employees/{employeeId}/disable")
    class DisableEmployee {

        @Test
        void shouldReturnNoContent_whenEmployeeIsDisabled() throws Exception {
            //given
            willDoNothing().given(employeeService).disableEmployeeById(employeeId);

            //when & then
            mockMvc.perform(patch(BASE_URL + "/{employeeId}/disable", employeeId))
                    .andExpect(status().isNoContent());
        }


        @Test
        void shouldReturnNotFound_whenEmployeeDoesNotExist() throws Exception {
            //given
            willThrow(new ResourceNotFoundException("Employee not found with id: " + employeeId))
                    .given(employeeService).disableEmployeeById(employeeId);

            //when & then
            mockMvc.perform(patch(BASE_URL + "/{employeeId}/disable", employeeId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Employee not found with id: " + employeeId));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/employees/{employeeId}/enable")
    class EnableEmployee {

        @Test
        void shouldReturnNoContent_whenEmployeeIsEnabled() throws Exception {
            //given
            willDoNothing().given(employeeService).enableEmployeeById(employeeId);

            //when & then
            mockMvc.perform(patch(BASE_URL + "/{employeeId}/enable", employeeId))
                    .andExpect(status().isNoContent());
        }


        @Test
        void shouldReturnNotFound_whenEmployeeDoesNotExist() throws Exception {
            //given
            willThrow(new ResourceNotFoundException("Employee not found with id: " + employeeId))
                    .given(employeeService).enableEmployeeById(employeeId);

            //when & then
            mockMvc.perform(patch(BASE_URL + "/{employeeId}/enable", employeeId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Employee not found with id: " + employeeId));
        }
    }
}
