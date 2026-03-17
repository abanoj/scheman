package com.abanoj.scheman.employee.entity;

import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.shared.BaseEntity;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "employee")
public class Employee extends BaseEntity {
    @Id
    private UUID id;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "employee")
    @Builder.Default
    private Set<ShiftAssignment> shiftAssignments = new HashSet<>();
    private Integer weeklyContractedHours;
}
