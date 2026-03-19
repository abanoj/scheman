package com.abanoj.scheman.employee.repository;

import com.abanoj.scheman.employee.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
}
