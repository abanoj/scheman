package com.abanoj.scheman.shift.repository;

import com.abanoj.scheman.shift.entity.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
}
