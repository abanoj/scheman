package com.abanoj.scheman.store;

import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.shift.entity.Shift;
import com.abanoj.scheman.store.dto.StoreCreateRequestDto;
import com.abanoj.scheman.store.dto.StoreListResponseDto;
import com.abanoj.scheman.store.dto.StoreResponseDto;
import com.abanoj.scheman.store.dto.StoreUpdateRequestDto;
import com.abanoj.scheman.store.entity.Store;
import com.abanoj.scheman.store.mapper.StoreMapper;
import com.abanoj.scheman.store.repository.StoreRepository;
import com.abanoj.scheman.store.service.StoreServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StoreServiceImplTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreMapper storeMapper;

    @InjectMocks
    private StoreServiceImpl storeService;

    private UUID storeId;
    private Store store;
    private StoreResponseDto storeResponseDto;

    @BeforeEach
    void setUp(){
        storeId = UUID.randomUUID();
        store = Store.builder()
                .id(storeId)
                .name("Puente Rojo")
                .deleted(false)
                .build();
        storeResponseDto = new StoreResponseDto(
                storeId,
                "Puente Rojo",
                null,
                null,
                null,
                null,
                null);
    }

    @Nested
    @DisplayName("findAllStores")
    class FindAllStores{
        @Test
        void shouldReturnPageOfStores_whenStoresExists(){
            //given
            StoreListResponseDto storeListResponseDto = new StoreListResponseDto(storeId, "Puente Rojo", null, null, null);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Store> shiftPage = new PageImpl<>(List.of(store));
            given(storeRepository.findAllByDeletedFalse(pageable)).willReturn(shiftPage);
            given(storeMapper.toListResponseDto(any(Store.class))).willReturn(storeListResponseDto);
            //when
            Page<StoreListResponseDto> result = storeService.findAllStores(pageable);
            //then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(storeListResponseDto);
        }

        @Test
        void shouldReturnEmptyPage_whenStoreDoesNotExist(){
            //given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Store> shiftPage = Page.empty();
            given(storeRepository.findAllByDeletedFalse(pageable)).willReturn(shiftPage);
            //when
            Page<StoreListResponseDto> result = storeService.findAllStores(pageable);
            //then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findStoreById")
    class FindStoreById {
        @Test
        void shouldReturnStore_whenExists(){
            //given
            given(storeRepository.findWithShiftsByIdAndDeletedFalse(storeId)).willReturn(Optional.of(store));
            given(storeMapper.toResponseDto(store)).willReturn(storeResponseDto);
            //when
            StoreResponseDto result = storeService.findStoreById(storeId);
            //then
            assertThat(result.id()).isEqualTo(storeId);
            assertThat(result.name()).isEqualTo("Puente Rojo");
        }

        @Test
        void shouldThrowResourceNotFound_whenStoreDoesNotExist(){
            //given
            UUID unknownId = UUID.randomUUID();
            given(storeRepository.findWithShiftsByIdAndDeletedFalse(unknownId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> storeService.findStoreById(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Store not found with id " + unknownId);
        }
    }

    @Nested
    @DisplayName("createStore")
    class CreateStore {
        @Test
        void shouldCreateStore(){
            //given
            StoreCreateRequestDto storeCreateRequestDto = new StoreCreateRequestDto("Puente Rojo", null, null, null);
            given(storeMapper.toStore(storeCreateRequestDto)).willReturn(store);
            given(storeRepository.save(store)).willReturn(store);
            given(storeMapper.toResponseDto(store)).willReturn(storeResponseDto);
            //when
            StoreResponseDto result = storeService.createStore(storeCreateRequestDto);
            //then
            verify(storeRepository).save(store);
            assertThat(result.id()).isEqualTo(storeId);
            assertThat(result.name()).isEqualTo("Puente Rojo");
        }
    }

    @Nested
    @DisplayName("updateStore")
    class UpdateStore {

        @Test
        void shouldUpdateStore_whenExists(){
            //given
            StoreUpdateRequestDto storeUpdateRequestDto = new StoreUpdateRequestDto(
                    "Puente Rojo",
                    null,
                    null,
                    false
            );
            StoreResponseDto storeUpdateResponseDto = new StoreResponseDto(
                    storeId,
                    "Puente Rojo",
                    null,
                    null,
                    false,
                    null,
                    null);
            given(storeRepository.findWithShiftsByIdAndDeletedFalse(storeId)).willReturn(Optional.of(store));
            willDoNothing().given(storeMapper).updateStoreFromDto(storeUpdateRequestDto, store);
            given(storeMapper.toResponseDto(store)).willReturn(storeUpdateResponseDto);
            //when
            StoreResponseDto result = storeService.updateStore(storeId, storeUpdateRequestDto);
            //then
            verify(storeRepository).save(store);
            assertThat(result.id()).isEqualTo(storeId);
            assertThat(result.is24h()).isFalse();
        }

        @Test
        void shouldThrowResourceNotFound_whenStoreDoesNotExist(){
            //given
            UUID unknownId = UUID.randomUUID();
            given(storeRepository.findWithShiftsByIdAndDeletedFalse(unknownId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> storeService.updateStore(unknownId, any(StoreUpdateRequestDto.class)))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Store not found with id " + unknownId);
            verify(storeMapper, never()).updateStoreFromDto(any(), any());
            verify(storeRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteStore")
    class DeleteStore {

        @Test
        void shouldDeleteStore_whenExists_andHasNoActiveShift(){
            //given
            given(storeRepository.findWithShiftsByIdAndDeletedFalse(storeId)).willReturn(Optional.of(store));
            //when
            storeService.deleteStore(storeId);
            //then
            assertThat(store.isDeleted()).isTrue();
            verify(storeRepository).save(store);
        }

        @Test
        void shouldThrowResourceNotFound_whenStoreDoesNotExist(){
            //given
            UUID unknownId = UUID.randomUUID();
            given(storeRepository.findWithShiftsByIdAndDeletedFalse(unknownId)).willReturn(Optional.empty());
            //when -> then
            assertThatThrownBy(() -> storeService.deleteStore(unknownId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Store not found with id " + unknownId);
            verify(storeRepository, never()).save(any());
            assertThat(store.isDeleted()).isFalse();
        }

        @Test
        void shouldThrowConflict_whenStoreHasActiveShifts(){
            //given
            Shift shift = Shift.builder().build();
            Store storeWithShifts = Store.builder()
                    .id(storeId)
                    .name("Puente Rojo")
                    .shifts(Set.of(shift))
                    .build();
            given(storeRepository.findWithShiftsByIdAndDeletedFalse(storeId)).willReturn(Optional.of(storeWithShifts));
            //when -> then
            assertThatThrownBy(() -> storeService.deleteStore(storeId))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("active shifts");
            verify(storeRepository, never()).save(any());
            assertThat(store.isDeleted()).isFalse();
        }
    }
}
