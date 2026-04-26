package com.abanoj.scheman.shiftassignment.service;

import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.employee.repository.EmployeeRepository;
import com.abanoj.scheman.exception.ConflictException;
import com.abanoj.scheman.exception.ResourceNotFoundException;
import com.abanoj.scheman.shift.entity.Shift;
import com.abanoj.scheman.shift.repository.ShiftRepository;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentCreateRequestDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentResponseDto;
import com.abanoj.scheman.shiftassignment.dto.ShiftAssignmentUpdateRequestDto;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import com.abanoj.scheman.shiftassignment.mapper.ShiftAssignmentMapper;
import com.abanoj.scheman.shiftassignment.repository.ShiftAssignmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShiftAssignmentServiceImpl implements ShiftAssignmentService{

    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final ShiftAssignmentMapper shiftAssignmentMapper;
    private final EmployeeRepository employeeRepository;
    private final ShiftRepository shiftRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftAssignmentResponseDto> findAllShiftsAssignmentsByShiftId(Pageable pageable, UUID shiftId) {
        return shiftAssignmentRepository
                .findAllByShiftId(pageable, shiftId)
                .map(shiftAssignmentMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShiftAssignmentResponseDto> findAllShiftsAssignmentsByEmployeeId(Pageable pageable, UUID employeeId) {
        return shiftAssignmentRepository
                .findAllByEmployeeId(pageable, employeeId)
                .map(shiftAssignmentMapper::toResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftAssignmentResponseDto findShiftAssignmentById(UUID shiftId, UUID id) {
        return shiftAssignmentMapper.toResponseDto(findShiftAssignmentOrThrow(shiftId, id));
    }

    @Override
    @Transactional
    public ShiftAssignmentResponseDto createShiftAssignment(UUID shiftId, ShiftAssignmentCreateRequestDto shiftAssignmentCreateRequestDto) {
        UUID employeeId = shiftAssignmentCreateRequestDto.employeeId();
        Shift shift = shiftRepository
                .findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found shift with id " + shiftId));
        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found employee with id " + employeeId));
        validateNoOverlap(employeeId, shiftAssignmentCreateRequestDto.date(), shift, null);

        ShiftAssignment shiftAssignment = shiftAssignmentMapper.toShiftAssignment(shiftAssignmentCreateRequestDto);
        shiftAssignment.setShift(shift);
        shiftAssignment.setEmployee(employee);
        shiftAssignmentRepository.save(shiftAssignment);
        log.debug("Created Shift Assignment with id {}", shiftAssignment.getId());
        return shiftAssignmentMapper.toResponseDto(shiftAssignment);
    }

    @Override
    @Transactional
    public ShiftAssignmentResponseDto updateShiftAssignment(UUID shiftId, UUID id, ShiftAssignmentUpdateRequestDto shiftAssignmentUpdateRequestDto) {
        UUID employeeId = shiftAssignmentUpdateRequestDto.employeeId();
        Shift shift = shiftRepository
                .findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found shift with id " + shiftId));
        ShiftAssignment shiftAssignment = shiftAssignmentRepository
                .findByIdAndShiftId(id, shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found shift assignment with id " + id));
        Employee employee = employeeRepository
                .findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found employee with id " + employeeId));
        validateNoOverlap(employeeId, shiftAssignmentUpdateRequestDto.date(), shift, id);

        shiftAssignmentMapper.updateShiftAssignmentFromDto(shiftAssignmentUpdateRequestDto, shiftAssignment);
        shiftAssignment.setShift(shift);
        shiftAssignment.setEmployee(employee);
        shiftAssignmentRepository.save(shiftAssignment);
        log.debug("Updated Shift Assignment with id {}", shiftAssignment.getId());
        return shiftAssignmentMapper.toResponseDto(shiftAssignment);
    }

    @Override
    @Transactional
    public void delete(UUID shiftId, UUID id) {
        if(!shiftAssignmentRepository.existsByIdAndShiftId(id, shiftId)){
            throw new ResourceNotFoundException("Not found shift assignment with id " + id);
        }
        shiftAssignmentRepository.deleteById(id);
        log.debug("Deleted Shift Assignment {}", id);
    }

    private void validateNoOverlap(UUID employeeId, LocalDate date, Shift newShift, UUID excludeAssignmentId) {
        List<LocalDate> datesToCheck = List.of(date, date.minusDays(1));

        List<ShiftAssignment> existing = shiftAssignmentRepository
                .findByEmployeeIdAndDateIn(employeeId, datesToCheck);

        for (ShiftAssignment assignment : existing) {
            if (excludeAssignmentId != null && excludeAssignmentId.equals(assignment.getId())) {
                continue;
            }
            if (overlaps(assignment.getShift(), assignment.getDate(), newShift, date)) {
                throw new ConflictException("Employee already has an overlapping shift on " + date);
            }
        }
    }

    private boolean overlaps(Shift a, LocalDate dateA, Shift b, LocalDate dateB) {
        LocalDateTime startA = dateA.atTime(a.getStartTime());
        LocalDateTime endA = a.isCrossesMidnight()
                ? dateA.plusDays(1).atTime(a.getEndTime())
                : dateA.atTime(a.getEndTime());

        LocalDateTime startB = dateB.atTime(b.getStartTime());
        LocalDateTime endB = b.isCrossesMidnight()
                ? dateB.plusDays(1).atTime(b.getEndTime())
                : dateB.atTime(b.getEndTime());

        return startA.isBefore(endB) && startB.isBefore(endA);
    }

    private ShiftAssignment findShiftAssignmentOrThrow(UUID shiftId, UUID id) {
        return shiftAssignmentRepository
                .findByIdAndShiftId(id, shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found shift assignment with id " + id));
    }
}
