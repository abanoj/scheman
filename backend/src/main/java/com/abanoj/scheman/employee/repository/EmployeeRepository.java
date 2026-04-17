package com.abanoj.scheman.employee.repository;

import com.abanoj.scheman.employee.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    @Query("SELECT e FROM Employee e JOIN FETCH e.user")
    Page<Employee> findAllWithUser(Pageable pageable);

    @Query("SELECT e FROM Employee e JOIN FETCH e.user WHERE e.id = :id")
    Optional<Employee> findByIdWithUser(UUID id);
}
