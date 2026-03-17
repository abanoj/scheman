package com.abanoj.scheman.shiftassignment.entity;

import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.shared.BaseEntity;
import com.abanoj.scheman.shift.entity.Shift;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shift_assignment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"employee_id", "shift_id", "date"})
})
public class ShiftAssignment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private LocalDate date;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;
}
