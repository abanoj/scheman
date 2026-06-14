package com.abanoj.scheman.auth;

import com.abanoj.scheman.auth.controller.AuthController;
import com.abanoj.scheman.auth.dto.*;
import com.abanoj.scheman.auth.service.AuthService;
import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.config.RateLimitProperties;
import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.abanoj.scheman.exception.AuthenticationFailedException;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private CorsProperties corsProperties;

    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    private static final String BASE_URL = "/api/v1/auth";

    @Nested
    @DisplayName("POST /api/v1/auth/login")
    class Login {
        @Test
        void shouldLogin_whenRequestIsValid() throws Exception{
            //given
            LoginRequestDto request = new LoginRequestDto("jesus@abanoj.com", "12345678");
            AuthResponseDto response = new AuthResponseDto("access-123", "refresh-123", false);
            given(authService.login(request)).willReturn(response);
            //when -> then
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.access_token").value("access-123"));
        }

        @Test
        void shouldReturnBadRequest_whenBodyIsInvalid() throws Exception{
            //given
            LoginRequestDto request = new LoginRequestDto("abanoj.com", "12345678");
            //when -> then
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception{
            //given
            LoginRequestDto request = new LoginRequestDto("jesus@abanoj.com", "12345678");
            given(authService.login(request)).willThrow(new AuthenticationFailedException("Invalid email or password"));
            //when -> then
            mockMvc.perform(post(BASE_URL + "/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/signup/manager")
    class CreateManager {
        @Test
        void shouldCreateManager_whenRequestIsValid() throws Exception {
            //given
            UUID managerId = UUID.randomUUID();
            ManagerCreateRequestDto request = new ManagerCreateRequestDto(
                    "Jesus",
                    "Abano",
                    "jesus@abanoj.com",
                    "m1-Password"
            );
            ManagerResponseDto response = new ManagerResponseDto(
                    managerId,
                    "Jesus",
                    "Abano",
                    "jesus@abanoj.com",
                    true
            );
            given(authService.createManager(request)).willReturn(response);
            //when -> then
            mockMvc.perform(post(BASE_URL + "/signup/manager")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(managerId.toString()));
        }

        @Test
        void shouldReturnBadRequest_whenBodyIsInvalid() throws Exception {
            //given
            ManagerCreateRequestDto request = new ManagerCreateRequestDto(
                    "Jesus",
                    "Abano",
                    "jesus@abanoj.com",
                    "123456"
            );
            //when -> then
            mockMvc.perform(post(BASE_URL + "/signup/manager")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnConflict_whenEmailIsAlreadyInUse() throws Exception {
            //given
            ManagerCreateRequestDto request = new ManagerCreateRequestDto(
                    "Jesus",
                    "Abano",
                    "jesus@abanoj.com",
                    "m1-Password"
            );
            given(authService.createManager(request)).willThrow(new ConflictException("Email already in use"));
            //when -> then
            mockMvc.perform(post(BASE_URL + "/signup/manager")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Email already in use"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/auth/{userId}/password")
    class ChangePassword {

        @Test
        void shouldChangePassword_whenRequestIsValid() throws Exception {
            //given
            UUID userId = UUID.randomUUID();
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    "oldPassword",
                    "M1-new-Password",
                    "M1-new-Password"
            );
            willDoNothing().given(authService).changePassword(userId, request);
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{userId}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturnBadRequest_whenBodyIsInvalid() throws Exception{
            //given
            UUID userId = UUID.randomUUID();
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    "oldPassword",
                    "M1-NewPassword",
                    "M2-NewPassword"
            );
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{userId}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnNotFound_wheUserDoesNotExist() throws Exception {
            //given
            UUID userId = UUID.randomUUID();
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    "oldPassword",
                    "M1-NewPassword",
                    "M1-NewPassword"
            );
            willThrow(new ResourceNotFoundException("Not found user with id " + userId)).given(authService).changePassword(userId, request);
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{userId}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Not found user with id " + userId));

        }

    }

    @Nested
    @DisplayName("POST /api/v1/auth/refresh")
    class Refresh {
        @Test
        void shouldRefreshToken_whenRequestIsValid() throws  Exception {
            //given
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("refresh-123");
            AuthResponseDto response = new AuthResponseDto("access-123", "refresh-123", false);
            given(authService.refresh(request)).willReturn(response);
            //when -> then
            mockMvc.perform(post(BASE_URL + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.refresh_token").value("refresh-123"));
        }

        @Test
        void shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
            //given
            RefreshTokenRequestDto request = new RefreshTokenRequestDto(null);
            //when -> then
            mockMvc.perform(post(BASE_URL + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
            //given
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("refresh-123");
            given(authService.refresh(request)).willThrow(new AuthenticationFailedException("Invalid refresh token"));
            //when -> then
            mockMvc.perform(post(BASE_URL + "/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class Logout {
        @Test
        void shouldLogout_whenRequestIsValid() throws Exception {
            //given
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("refresh-123");
            willDoNothing().given(authService).logout(request);
            //when -> then
            mockMvc.perform(post(BASE_URL + "/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
            //given
            RefreshTokenRequestDto request = new RefreshTokenRequestDto(null);
            //when -> then
            mockMvc.perform(post(BASE_URL + "/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturnUnauthorized_whenTokenIsInvalid() throws Exception {
            //given
            RefreshTokenRequestDto request = new RefreshTokenRequestDto("invalid-123");
            willThrow(new AuthenticationFailedException("Invalid refresh token")).given(authService).logout(request);
            //when -> then
            mockMvc.perform(post(BASE_URL + "/logout")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Invalid refresh token"));

        }
    }
}
