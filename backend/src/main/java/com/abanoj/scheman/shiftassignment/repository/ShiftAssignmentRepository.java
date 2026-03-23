package com.abanoj.scheman.shiftassignment.repository;

import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {
    List<ShiftAssignment> findByShiftId(UUID id);
    List<ShiftAssignment> findByEmployeeId(UUID id);
}
