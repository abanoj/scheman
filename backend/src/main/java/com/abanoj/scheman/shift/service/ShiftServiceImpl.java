package com.abanoj.scheman.shift.service;

import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.shift.dto.ShiftCreateRequestDto;
import com.abanoj.scheman.shift.dto.ShiftResponseDto;
import com.abanoj.scheman.shift.dto.ShiftUpdateRequestDto;
import com.abanoj.scheman.shift.dto.UnassignedShiftResponseDto;
import com.abanoj.scheman.shift.entity.Shift;
import com.abanoj.scheman.shift.mapper.ShiftMapper;
import com.abanoj.scheman.shift.repository.ShiftRepository;
import com.abanoj.scheman.shiftassignment.repository.ShiftAssignmentRepository;
import com.abanoj.scheman.store.entity.Store;
import com.abanoj.scheman.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShiftServiceImpl implements ShiftService{

    private final ShiftRepository shiftRepository;
    private final StoreRepository storeRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftMapper shiftMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftResponseDto> findAllShiftsByStoreId(Pageable pageable, UUID storeId) {
        return shiftRepository
                .findAllByStoreIdAndDeletedFalse(pageable, storeId)
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
        shift.setGroupId(UUID.randomUUID());
        shiftRepository.save(shift);
        log.debug("Created Shift with id {}", shift.getId());
        return shiftMapper.toResponseDto(shift);
    }

    @Override
    @Transactional
    public ShiftResponseDto updateShift(UUID storeId, UUID id, ShiftUpdateRequestDto shiftUpdateRequestDto) {
        Shift shift = findShiftOrThrow(id, storeId);
        shift.setEffectiveTo(LocalDate.now().minusDays(1));
        shift.setDeleted(true);

        Shift newShift = Shift.builder()
                .name(shiftUpdateRequestDto.name())
                .store(shift.getStore())
                .startTime(shiftUpdateRequestDto.startTime())
                .endTime(shiftUpdateRequestDto.endTime())
                .availableDays(shiftUpdateRequestDto.availableDays())
                .groupId(shift.getGroupId())
                .effectiveFrom(LocalDate.now())
                .shiftType(shift.getShiftType())
                .build();

        shiftRepository.saveAll(List.of(shift, newShift));
        log.debug("Updated Shift group with id {}", shift.getGroupId());
        return shiftMapper.toResponseDto(newShift);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnassignedShiftResponseDto> findUnassignedShifts(UUID storeId, LocalDate date) {
        LocalDate monday = date.with(DayOfWeek.MONDAY);
        LocalDate sunday = date.with(DayOfWeek.SUNDAY);

        List<Shift> activeShifts = shiftRepository.findActiveShiftsByStoreIdAndDateRange(storeId, monday, sunday);

        if (activeShifts.isEmpty()) {
            return List.of();
        }

        List<UUID> activeShiftIds = activeShifts.stream().map(Shift::getId).toList();
        Set<String> assignedKeys = shiftAssignmentRepository
                .findByShiftIdInAndDateBetween(activeShiftIds, monday, sunday)
                .stream()
                .map(sa -> sa.getShift().getId() + ":" + sa.getDate())
                .collect(Collectors.toSet());

        List<UnassignedShiftResponseDto> unassignedShift = new ArrayList<>();
        for (Shift shift : activeShifts) {
            Set<DayOfWeek> unassignedDays = new HashSet<>();
            for (LocalDate currentDate = monday; !currentDate.isAfter(sunday); currentDate = currentDate.plusDays(1)) {
                if (!shift.getAvailableDays().contains(currentDate.getDayOfWeek())) continue;
                if (currentDate.isBefore(shift.getEffectiveFrom())) continue;
                if (shift.getEffectiveTo() != null && currentDate.isAfter(shift.getEffectiveTo())) continue;
                String key = shift.getId() + ":" + currentDate;
                if (!assignedKeys.contains(key)) {
                    unassignedDays.add(currentDate.getDayOfWeek());
                }
            }
            if(!unassignedDays.isEmpty()){
                unassignedShift.add(new UnassignedShiftResponseDto(shift.getId(), shift.getName(), unassignedDays));
            }
        }
        return unassignedShift;
    }

    @Override
    @Transactional
    public void deleteShift(UUID storeId, UUID id) {
        Shift shift = findShiftOrThrow(id, storeId);
        shift.setEffectiveTo(LocalDate.now().minusDays(1));
        shift.setDeleted(true);
        shiftRepository.save(shift);
        log.debug("Shift {} deleted", id);
    }

    private Shift findShiftOrThrow(UUID id, UUID storeId){
        return shiftRepository
                .findByIdAndStoreIdAndDeletedFalse(id, storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found shift with id " + id));
    }
}
