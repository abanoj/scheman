package com.abanoj.scheman.auth;

import com.abanoj.scheman.auth.controller.AuthController;
import com.abanoj.scheman.auth.dto.ChangePasswordRequestDto;
import com.abanoj.scheman.auth.dto.ManagerCreateRequestDto;
import com.abanoj.scheman.auth.entity.Role;
import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.auth.service.AuthService;
import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.config.RateLimitProperties;
import com.abanoj.scheman.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(AuthControllerSecurityTest.TestMethodSecurityConfig.class)
@DisplayName("AuthController - Security")
public class AuthControllerSecurityTest {
    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

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
    @DisplayName("Authorized")
    class Authorized {
        @Test
        void shouldReturn204_whenUserChangeIsOwnPassword() throws Exception {
            //given
            UUID userId = UUID.randomUUID();
            User user = User.builder()
                    .id(userId)
                    .firstName("Jesus")
                    .lastName("Abano")
                    .email("jesus@abanoj.com")
                    .password("password")
                    .role(Role.EMPLOYEE)
                    .build();
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    "oldPassword",
                    "M1-new-Password",
                    "M1-new-Password"
            );
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{userId}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                    .andExpect(status().isNoContent());
        }

    }

    @Nested
    @DisplayName("Unauthenticated requests")
    class Unauthenticated {

        @Test
        void shouldReturn401_whenAccessingCreateManagerWithoutAuth() throws Exception{
            //given
            ManagerCreateRequestDto request = new ManagerCreateRequestDto(
                    "Jesus",
                    "Abano",
                    "jesus@abanoj.com",
                    "m1-Password"
            );
            //when -> then
            mockMvc.perform(post(BASE_URL + "/signup/manager")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingChangePasswordWithoutAuth() throws Exception{
            //given
            UUID userId = UUID.randomUUID();
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    "oldPassword",
                    "M1-new-Password",
                    "M1-new-Password"
            );
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{userId}/password", userId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Forbidden")
    class Forbidden {

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        void shouldReturn403_whenEmployeeAccessesCreateManager() throws Exception {
            //given
            ManagerCreateRequestDto request = new ManagerCreateRequestDto(
                    "Jesus",
                    "Abano",
                    "jesus@abanoj.com",
                    "m1-Password"
            );
            //when -> then
            mockMvc.perform(post(BASE_URL + "/signup/manager")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "MANAGER")
        void shouldReturn403_whenManagerAccessesCreateManager() throws Exception {
            //given
            ManagerCreateRequestDto request = new ManagerCreateRequestDto(
                    "Jesus",
                    "Abano",
                    "jesus@abanoj.com",
                    "m1-Password"
            );
            //when -> then
            mockMvc.perform(post(BASE_URL + "/signup/manager")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenUserTryToChangeAnotherUserPassword() throws Exception {
            //given
            UUID userId = UUID.randomUUID();
            User user = User.builder()
                    .id(userId)
                    .firstName("Jesus")
                    .lastName("Abano")
                    .email("jesus@abanoj.com")
                    .password("password")
                    .role(Role.EMPLOYEE)
                    .build();
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    "oldPassword",
                    "M1-new-Password",
                    "M1-new-Password"
            );
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{userId}/password", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn403_whenAdminTryToChangeAnotherUserPassword() throws Exception {
            //given
            UUID userId = UUID.randomUUID();
            User admin = User.builder()
                    .id(userId)
                    .firstName("Jesus")
                    .lastName("Abano")
                    .email("jesus@abanoj.com")
                    .password("password")
                    .role(Role.ADMIN)
                    .build();
            ChangePasswordRequestDto request = new ChangePasswordRequestDto(
                    "oldPassword",
                    "M1-new-Password",
                    "M1-new-Password"
            );
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{userId}/password", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            .with(csrf())
                            .with(SecurityMockMvcRequestPostProcessors.authentication(authentication)))
                    .andExpect(status().isForbidden());
        }

    }

}
