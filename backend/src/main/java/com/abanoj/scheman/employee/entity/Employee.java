package com.abanoj.scheman.employee.entity;

import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.shared.BaseEntity;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "employees")
public class Employee extends BaseEntity {
    @Id
    private UUID id;
    @Column(unique = true)
    private String dni;
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ShiftAssignment> shiftAssignments = new HashSet<>();
    private Integer weeklyContractedHours;
    @Enumerated(EnumType.STRING)
    private ShiftType preferredShift;

}
