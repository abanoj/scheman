package com.abanoj.scheman.shift.service;

import com.abanoj.scheman.shift.dto.ShiftCreateRequestDto;
import com.abanoj.scheman.shift.dto.ShiftResponseDto;
import com.abanoj.scheman.shift.dto.ShiftUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShiftService {
    Page<ShiftResponseDto> findAllShiftsByStoreId(Pageable pageable, UUID storeId);
    ShiftResponseDto findShiftById(UUID id, UUID storeId);
    ShiftResponseDto createShift(UUID storeId, ShiftCreateRequestDto shiftCreateRequestDto);
    ShiftResponseDto updateShift(UUID storeId, UUID id, ShiftUpdateRequestDto shiftUpdateRequestDto);
    void deleteShift(UUID storeId, UUID id);
}
