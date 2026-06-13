package com.abanoj.scheman.shiftassignment.repository;

import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, UUID> {
    @EntityGraph(attributePaths = {"employee", "shift"})
    Page<ShiftAssignment> findAllByShiftId(Pageable pageable, UUID shiftId);

    @EntityGraph(attributePaths = {"employee", "shift"})
    Page<ShiftAssignment> findAllByEmployeeId(Pageable pageable, UUID employeeId);
    Optional<ShiftAssignment> findByIdAndShiftId(UUID id, UUID shiftId);
    boolean existsByIdAndShiftId(UUID id, UUID shiftId);

    @Query("""
            SELECT sa FROM ShiftAssignment sa
            JOIN FETCH sa.shift s
            JOIN FETCH sa.employee e
            WHERE e.id = :employeeId
            AND sa.date BETWEEN :startDate AND :endDate
            ORDER BY sa.date ASC, s.startTime ASC
            """)
    List<ShiftAssignment> findByEmployeeIdAndDateBetween(UUID employeeId, LocalDate startDate, LocalDate endDate);

    @Query("""
           SELECT sa FROM ShiftAssignment sa
           JOIN FETCH sa.shift s
           WHERE s.id IN :shiftIds
           AND sa.date BETWEEN :startDate AND :endDate
           """)
    List<ShiftAssignment> findByShiftIdInAndDateBetween(List<UUID> shiftIds, LocalDate startDate, LocalDate endDate);

    @Query("""
           SELECT sa FROM ShiftAssignment sa
           JOIN FETCH sa.shift s
           JOIN FETCH sa.employee e
           JOIN FETCH e.user u
           WHERE s.id IN :shiftIds
           AND sa.date BETWEEN :startDate AND :endDate
           """)
    List<ShiftAssignment> findWithEmployeeByShiftIdInAndDateBetween(List<UUID> shiftIds, LocalDate startDate, LocalDate endDate);
}
