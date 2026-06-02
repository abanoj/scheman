package com.abanoj.scheman.store;

import com.abanoj.scheman.config.CorsProperties;
import com.abanoj.scheman.config.RateLimitProperties;
import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.security.JwtService;
import com.abanoj.scheman.store.controller.StoreController;
import com.abanoj.scheman.store.dto.StoreCreateRequestDto;
import com.abanoj.scheman.store.dto.StoreListResponseDto;
import com.abanoj.scheman.store.dto.StoreResponseDto;
import com.abanoj.scheman.store.dto.StoreUpdateRequestDto;
import com.abanoj.scheman.store.service.StoreService;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(StoreController.class)
@AutoConfigureMockMvc(addFilters = false)
class StoreControllerTest {
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

    private UUID storeId;
    private StoreResponseDto storeResponseDto;

    @BeforeEach
    void setUp(){
        storeId = UUID.randomUUID();
        storeResponseDto = new StoreResponseDto(
                storeId,
                "San Blas",
                "Pl. General Mancha, 1",
                "965910223",
                false,
                null,
                null
        );
    }

    @Nested
    @DisplayName("GET /api/v1/stores")
    class GetAllStores{

        private StoreListResponseDto storeListResponseDto;

        @BeforeEach
        void setUp(){
                storeListResponseDto = new StoreListResponseDto(
                        UUID.randomUUID(),
                        "San Blas",
                        "Pl. General Mancha, 1",
                        "965910223",
                        false
                );
        }

        @Test
        void shouldReturnPageOfStores_whenStoresExists() throws Exception {
            //given
            Page<StoreListResponseDto> page = new PageImpl<>(List.of(storeListResponseDto));
            given(storeService.findAllStores(any(Pageable.class))).willReturn(page);
            //when -> then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(storeListResponseDto.id().toString()))
                    .andExpect(jsonPath("$.content[0].name").value(storeListResponseDto.name()))
                    .andExpect(jsonPath("$.content[0].address").value(storeListResponseDto.address()))
                    .andExpect(jsonPath("$.content[0].phone").value(storeListResponseDto.phone()))
                    .andExpect(jsonPath("$.content[0].is24h").value(storeListResponseDto.is24h()));
        }

        @Test
        void shouldReturnEmptyPage_whenStoreDoesNotExist() throws Exception {
            //given
            Page<StoreListResponseDto> page = Page.empty();
            given(storeService.findAllStores(any(Pageable.class))).willReturn(page);
            //when -> then
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/stores/{id}")
    class GetStoreById {

        @Test
        void shouldReturnStore_whenExists() throws Exception {
            //given
            given(storeService.findStoreById(storeId)).willReturn(storeResponseDto);
            //when -> then
            mockMvc.perform(get(BASE_URL + "/{id}", storeId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(storeResponseDto.id().toString()))
                    .andExpect(jsonPath("$.name").value(storeResponseDto.name()));
        }

        @Test
        void shouldReturnNotFound_whenStoreDoesNotExist() throws Exception {
            //given
            given(storeService.findStoreById(storeId))
                    .willThrow(new ResourceNotFoundException("Store not found with id " + storeId));
            //when -> then
            mockMvc.perform(get(BASE_URL + "/{id}", storeId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Store not found with id " + storeId));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/stores")
    class CreateStore {
        private StoreCreateRequestDto storeCreateRequestDto;

        @BeforeEach
        void setUp(){
            storeCreateRequestDto = new StoreCreateRequestDto(
                    "San Blas",
                    "Pl. General Mancha, 1",
                    "965910223",
                    false
                    );
        }

        @Test
        void shouldCreateStore_whenRequestIsValid() throws Exception {
            //given
            given(storeService.createStore(storeCreateRequestDto)).willReturn(storeResponseDto);
            //when -> then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(storeCreateRequestDto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(storeResponseDto.id().toString()))
                    .andExpect(jsonPath("$.name").value(storeResponseDto.name())
                    );
        }

        @Test
        void shouldReturnBadRequest_whenNameIsInvalid() throws Exception {
            //given
            StoreCreateRequestDto invalidRequest = new StoreCreateRequestDto(
                    "",
                    "Pl. General Mancha, 1",
                    "965910223",
                    false
            );
            //when -> then
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("One or more fields have validation errors"));
        }

    }

    @Nested
    @DisplayName("PATCH /api/v1/stores/{id}")
    class UpdateStore {

        @Test
        void shouldUpdateStore_whenRequestIsValid() throws Exception{
            //given
            final StoreUpdateRequestDto storeUpdateRequestDto = new StoreUpdateRequestDto(
                    null,
                    null,
                    null,
                    true
            );
            final StoreResponseDto storeUpdateResponseDto = new StoreResponseDto(
                    storeId,
                    "San Blas",
                    "Pl. General Mancha, 1",
                    "965910223",
                    true,
                    null,
                    null
            );
            given(storeService.updateStore(storeId, storeUpdateRequestDto)).willReturn(storeUpdateResponseDto);
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{id}", storeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(storeUpdateRequestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(storeUpdateResponseDto.id().toString()))
                    .andExpect(jsonPath("$.name").value(storeUpdateResponseDto.name()))
                    .andExpect(jsonPath("$.is24h").value(true));
        }

        @Test
        void shouldReturnBadRequest_whenPhoneIsInvalid() throws Exception{
            //given
            final StoreUpdateRequestDto invalidRequest = new StoreUpdateRequestDto(null, null, "test-123", null);
            //when -> then
            mockMvc.perform(patch(BASE_URL + "/{id}", storeId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("One or more fields have validation errors"));
        }

       @Test
        void shouldReturnNotFound_whenStoreDoesNotExist() throws Exception {
            //given
           final StoreUpdateRequestDto storeUpdateRequestDto = new StoreUpdateRequestDto(
                   null,
                   null,
                   null,
                   true
           );
           UUID anotherStoreId = UUID.randomUUID();
           given(storeService.updateStore(anotherStoreId, storeUpdateRequestDto))
                   .willThrow(new ResourceNotFoundException("Store not found with id " + anotherStoreId));
           //when -> then
           mockMvc.perform(patch(BASE_URL + "/{id}", anotherStoreId)
                           .contentType(MediaType.APPLICATION_JSON)
                           .content(objectMapper.writeValueAsString(storeUpdateRequestDto)))
                   .andExpect(status().isNotFound())
                   .andExpect(jsonPath("$.message").value("Store not found with id " + anotherStoreId));


        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/stores/{id}")
    class DeleteStore {

        @Test
        void shouldReturnNoContent_whenStoreIsDeleted() throws Exception{
            //given
            willDoNothing().given(storeService).deleteStore(storeId);
            //when -> then
            mockMvc.perform(delete(BASE_URL + "/{id}", storeId))
                    .andExpect(status().isNoContent());
        }

        @Test
        void shouldReturnNotFound_whenStoreDoesNotExist() throws Exception{
            //given
            willThrow(new ResourceNotFoundException("Store not found with id " + storeId))
                    .given(storeService).deleteStore(storeId);
            // when -> then
            mockMvc.perform(delete(BASE_URL + "/{id}", storeId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Store not found with id " + storeId));
        }

        @Test
        void shouldReturnConflict_whenStoreHasAssociatedShifts() throws Exception{
            //given
            willThrow(new ConflictException("Cannot delete store with active shifts"))
                    .given(storeService).deleteStore(storeId);
            // when -> then
            mockMvc.perform(delete(BASE_URL + "/{id}", storeId))
                    .andExpect(status().isConflict());
        }

    }
}
