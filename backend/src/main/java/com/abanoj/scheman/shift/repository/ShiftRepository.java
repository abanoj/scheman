package com.abanoj.scheman.shift.repository;

import com.abanoj.scheman.shift.entity.Shift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    Optional<Shift> findByIdAndStoreIdAndDeletedFalse(UUID id, UUID storeId);
    Page<Shift> findAllByStoreIdAndDeletedFalse(Pageable pageable, UUID storeId);
    @Query("""
            SELECT s FROM Shift s
            WHERE s.groupId = :groupId AND s.effectiveFrom <= :date
            AND (s.effectiveTo IS NULL OR s.effectiveTo >= :date)
            AND s.deleted = false
           """)
    Optional<Shift> findActiveShiftByGroupId(@Param("groupId") UUID groupId, @Param("date") LocalDate date);
}
