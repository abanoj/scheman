package com.abanoj.scheman.store.repository;

import com.abanoj.scheman.store.entity.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    Page<Store> findAllByDeletedFalse(Pageable pageable);

    Optional<Store> findByIdAndDeletedFalse(UUID id);

    @EntityGraph(attributePaths = {"shifts", "preferredEmployees", "preferredEmployees.user"})
    Optional<Store> findWithShiftsByIdAndDeletedFalse(UUID id);
}
