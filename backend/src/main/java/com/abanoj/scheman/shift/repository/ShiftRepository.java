package com.abanoj.scheman.shift.repository;

import com.abanoj.scheman.shift.entity.Shift;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    Optional<Shift> findByIdAndStoreId(UUID id, UUID storeId);
    Page<Shift> findAllByStoreId(Pageable pageable, UUID storeId);
    boolean existsByIdAndStoreId(UUID id, UUID storeId);
}
