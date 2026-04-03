package com.abanoj.scheman.shift.service;

import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.shift.dto.ShiftCreateRequestDto;
import com.abanoj.scheman.shift.dto.ShiftResponseDto;
import com.abanoj.scheman.shift.dto.ShiftUpdateRequestDto;
import com.abanoj.scheman.shift.entity.Shift;
import com.abanoj.scheman.shift.mapper.ShiftMapper;
import com.abanoj.scheman.shift.repository.ShiftRepository;
import com.abanoj.scheman.store.entity.Store;
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
public class ShiftServiceImpl implements ShiftService{

    private final ShiftRepository shiftRepository;
    private final StoreRepository storeRepository;
    private final ShiftMapper shiftMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftResponseDto> findAllShiftsByStoreId(Pageable pageable, UUID storeId) {
        return shiftRepository
                .findAllByStoreId(pageable, storeId)
                .map(shiftMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponseDto findShiftById(UUID id, UUID storeId) {
        return shiftMapper.toResponseDto(findShiftOrThrow(id, storeId));
    }

    @Override
    @Transactional
    public ShiftResponseDto createShift(UUID storeId, ShiftCreateRequestDto shiftCreateRequestDto) {
        Store store = storeRepository
                .findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found store with id " + storeId));
        Shift shift = shiftMapper.toShift(shiftCreateRequestDto);
        shift.setStore(store);
        shiftRepository.save(shift);
        log.info("Created Shift with id {}", shift.getId());
        return shiftMapper.toResponseDto(shift);
    }

    @Override
    @Transactional
    public ShiftResponseDto updateShift(UUID storeId, UUID id, ShiftUpdateRequestDto shiftUpdateRequestDto) {
        Store store = storeRepository
                .findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found store with id " + storeId));
        Shift shift = findShiftOrThrow(id, storeId);
        shiftMapper.updateShiftFromDto(shiftUpdateRequestDto, shift);
        shift.setStore(store);
        shiftRepository.save(shift);
        log.info("Updated Shift with id {}", shift.getId());
        return shiftMapper.toResponseDto(shift);
    }

    @Override
    @Transactional
    public void deleteShift(UUID storeId, UUID id) {
        if(!shiftRepository.existsByIdAndStoreId(id, storeId)){
            throw new ResourceNotFoundException("Not found shift with id " + id);
        }
        shiftRepository.deleteById(id);
        log.info("Shift {} deleted", id);
    }

    private Shift findShiftOrThrow(UUID id, UUID storeId){
        return shiftRepository
                .findByIdAndStoreId(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found shift with id " + id));
    }
}
