package com.abanoj.scheman.store.repository;

import com.abanoj.scheman.store.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoreRepository extends JpaRepository<Store, UUID> {
}
