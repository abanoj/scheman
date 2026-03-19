package com.abanoj.scheman.store.service;

import com.abanoj.scheman.store.dto.StoreCreateRequest;
import com.abanoj.scheman.store.dto.StoreResponse;
import com.abanoj.scheman.store.dto.StoreUpdateRequest;
import com.abanoj.scheman.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface StoreService {
    Page<Store> findAllStore(Pageable pageable);
    StoreResponse findStoreById(UUID id);
    StoreResponse createStore(StoreCreateRequest store);
    StoreResponse updateStore(UUID id, StoreUpdateRequest store);
    void deleteStore(UUID id);
}
