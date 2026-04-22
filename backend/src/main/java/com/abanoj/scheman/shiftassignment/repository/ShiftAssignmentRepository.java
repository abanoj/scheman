package com.abanoj.scheman.shiftassignment.repository;

import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {
    Page<ShiftAssignment> findAllByShiftId(Pageable pageable, UUID shiftId);
    Page<ShiftAssignment> findAllByEmployeeId(Pageable pageable, UUID employeeId);
    Optional<ShiftAssignment> findByIdAndShiftId(UUID id, UUID shiftId);
    boolean existsByIdAndShiftId(UUID id, UUID shiftId);

    @Query("""
            SELECT sa FROM ShiftAssignment sa
            JOIN FETCH sa.shift
            WHERE sa.employee.id = :employeeId
            AND sa.date IN :dates
            """)
    List<ShiftAssignment> findByEmployeeIdAndDateIn(UUID employeeId, List<LocalDate> dates);
}
