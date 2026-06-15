package com.abanoj.scheman.employee.repository;

import com.abanoj.scheman.employee.entity.Employee;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    @Override
    @EntityGraph(attributePaths = {"user", "preferredStores"})
    Page<Employee> findAll(@NonNull Pageable pageable);

    @Query("SELECT e FROM Employee e JOIN FETCH e.user LEFT JOIN FETCH e.preferredStores WHERE e.id = :id")
    Optional<Employee> findByIdWithUser(UUID id);

    @Query("SELECT e FROM Employee e JOIN FETCH e.user u WHERE u.enabled = true ORDER BY u.lastName ASC, u.firstName ASC")
    List<Employee> findAllEnabledWithUser();
}
