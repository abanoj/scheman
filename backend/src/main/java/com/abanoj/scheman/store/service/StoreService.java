package com.abanoj.scheman.store.service;

import com.abanoj.scheman.store.dto.StoreCreateRequestDto;
import com.abanoj.scheman.store.dto.StoreResponseDto;
import com.abanoj.scheman.store.dto.StoreUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoreService {
    Page<StoreResponseDto> findAllStore(Pageable pageable);
    StoreResponseDto findStoreById(UUID id);
    StoreResponseDto createStore(StoreCreateRequestDto store);
    StoreResponseDto updateStore(UUID id, StoreUpdateRequestDto store);
    void deleteStore(UUID id);
}
