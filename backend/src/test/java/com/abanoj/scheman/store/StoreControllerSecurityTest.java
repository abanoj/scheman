package com.abanoj.scheman.store;

import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.config.RateLimitProperties;
import com.abanoj.scheman.security.JwtService;
import com.abanoj.scheman.store.controller.StoreController;
import com.abanoj.scheman.store.dto.StoreCreateRequestDto;
import com.abanoj.scheman.store.dto.StoreUpdateRequestDto;
import com.abanoj.scheman.store.service.StoreService;
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

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StoreController.class)
@Import(StoreControllerSecurityTest.TestMethodSecurityConfig.class)
@DisplayName("StoreController - Security")
public class StoreControllerSecurityTest {

    @EnableMethodSecurity
    static class TestMethodSecurityConfig {
    }
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StoreService storeService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private AuthenticationProvider authenticationProvider;

    @MockitoBean
    private CorsProperties corsProperties;

    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    private static final String BASE_URL = "/api/v1/stores";

    @Nested
    @DisplayName("Unauthenticated request")
    class Unauthenticated {
        @Test
        void shouldReturn401_whenAccessingGetAllStoresWithoutAuth() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingGetStoreByIdWithoutAuth() throws Exception{
            mockMvc.perform(get(BASE_URL + "/{id}", UUID.randomUUID()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingCreateStoreWithoutAuth() throws Exception{
            //given
            StoreCreateRequestDto storeCreateRequestDto = new StoreCreateRequestDto("San Blas", null, null, null);
            //when -> then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(storeCreateRequestDto))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingUpdateStoreWithoutAuth() throws Exception{
            //given
            StoreUpdateRequestDto storeUpdateRequestDto = new StoreUpdateRequestDto("San Blas", null, null, true);
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{id}", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(storeUpdateRequestDto))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldReturn401_whenAccessingDeleteStoreWithoutAuth() throws Exception{
            mockMvc.perform(delete(BASE_URL + "/{id}", UUID.randomUUID())
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Forbidden for Employee request")
    @WithMockUser(roles = "EMPLOYEE")
    class ForbiddenForEmployee {
        @Test
        void shouldReturn403_whenEmployeeAccessesGetAllStores() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401_whenEmployeeAccessesGetStoreById() throws Exception{
            mockMvc.perform(get(BASE_URL + "/{id}", UUID.randomUUID()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401_whenEmployeeAccessesCreateStore() throws Exception{
            //given
            StoreCreateRequestDto storeCreateRequestDto = new StoreCreateRequestDto("San Blas", null, null, null);
            //when -> then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(storeCreateRequestDto))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401_whenEmployeeAccessesUpdateStore() throws Exception{
            //given
            StoreUpdateRequestDto storeUpdateRequestDto = new StoreUpdateRequestDto("San Blas", null, null, true);
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{id}", UUID.randomUUID())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(storeUpdateRequestDto))
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldReturn401_whenEmployeeAccessesDeleteStore() throws Exception{
            mockMvc.perform(delete(BASE_URL + "/{id}", UUID.randomUUID())
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

    }
}
