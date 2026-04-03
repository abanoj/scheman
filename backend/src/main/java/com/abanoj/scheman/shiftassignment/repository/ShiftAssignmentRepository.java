package com.abanoj.scheman.shiftassignment.repository;

import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {
    Page<ShiftAssignment> findAllByShiftId(Pageable pageable, UUID shiftId);
    Page<ShiftAssignment> findAllByEmployeeId(Pageable pageable, UUID employeeId);
    Optional<ShiftAssignment> findByIdAndShiftId(UUID id, UUID shiftId);
    boolean existsByIdAndShiftId(UUID id, UUID shiftId);
}
