package com.abanoj.scheman.store.repository;

import com.abanoj.scheman.store.entity.Store;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
    @Override
    Page<Store> findAll(@NonNull Pageable pageable);
}
