package com.abanoj.scheman.store.service;

import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.store.dto.StoreCreateRequestDto;
import com.abanoj.scheman.store.dto.StoreResponseDto;
import com.abanoj.scheman.store.dto.StoreUpdateRequestDto;
import com.abanoj.scheman.store.entity.Store;
import com.abanoj.scheman.store.mapper.StoreMapper;
import com.abanoj.scheman.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoreServiceImpl implements StoreService{

    private final StoreRepository storeRepository;
    private final StoreMapper storeMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<StoreResponseDto> findAllStores(Pageable pageable) {
        return storeRepository
                .findAll(pageable)
                .map(storeMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponseDto findStoreById(UUID id) {
        return storeMapper.toResponseDto(findStoreOrThrow(id));
    }

    @Override
    @Transactional
    public StoreResponseDto createStore(StoreCreateRequestDto storeCreateRequestDto) {
        Store store = storeMapper.toStore(storeCreateRequestDto);
        StoreResponseDto storeResponseDto = storeMapper.toResponseDto(storeRepository.save(store));
        log.debug("Store created with id {}", storeResponseDto.id());
        return storeResponseDto;
    }

    @Override
    @Transactional
    public StoreResponseDto updateStore(UUID id, StoreUpdateRequestDto storeUpdateRequestDto) {
        Store store = findStoreOrThrow(id);
        storeMapper.updateStoreFromDto(storeUpdateRequestDto, store);
        storeRepository.save(store);
        log.debug("Updated store with id {}", store.getId());
        return storeMapper.toResponseDto(store);
    }

    @Override
    @Transactional
    public void deleteStore(UUID id) {
        if(!storeRepository.existsById(id)){
            throw new ResourceNotFoundException("Store not found with id " + id);
        }

        storeRepository.deleteById(id);
        log.debug("Store {} deleted", id);
    }

    private Store findStoreOrThrow(UUID id){
        return storeRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Store not found with id " + id));
    }

}

