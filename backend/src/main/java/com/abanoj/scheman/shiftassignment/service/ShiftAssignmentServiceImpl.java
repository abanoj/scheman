package com.abanoj.scheman.shiftassignment.service;

import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.employee.repository.EmployeeRepository;
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
        ShiftAssignment shiftAssignment = shiftAssignmentMapper.toShiftAssignment(shiftAssignmentCreateRequestDto);
        shiftAssignment.setShift(shift);
        shiftAssignment.setEmployee(employee);
        shiftAssignmentRepository.save(shiftAssignment);
        log.info("Created Shift Assignment with id {}", shiftAssignment.getId());
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
        shiftAssignmentMapper.updateShiftAssignmentFromDto(shiftAssignmentUpdateRequestDto, shiftAssignment);
        shiftAssignment.setShift(shift);
        shiftAssignment.setEmployee(employee);
        shiftAssignmentRepository.save(shiftAssignment);
        log.info("Updated Shift Assignment with id {}", shiftAssignment.getId());
        return shiftAssignmentMapper.toResponseDto(shiftAssignment);
    }

    @Override
    @Transactional
    public void delete(UUID shiftId, UUID id) {
        if(!shiftAssignmentRepository.existsByIdAndShiftId(id, shiftId)){
            throw new ResourceNotFoundException("Not found shift assignment with id " + id);
        }
        shiftAssignmentRepository.deleteById(id);
        log.info("Deleted Shift Assignment {}", id);
    }

    private ShiftAssignment findShiftAssignmentOrThrow(UUID shiftId, UUID id){
        return shiftAssignmentRepository
                .findByIdAndShiftId(id, shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Not found shift assignment with id " + id));
    }
}
