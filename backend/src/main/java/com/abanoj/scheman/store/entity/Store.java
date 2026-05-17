package com.abanoj.scheman.store.entity;

import com.abanoj.scheman.employee.entity.Employee;
import com.abanoj.scheman.shared.BaseEntity;
import com.abanoj.scheman.shift.entity.Shift;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLRestriction;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "stores")
public class Store extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    private String address;
    private Boolean is24h;
    private String phone;
    @Builder.Default
    private boolean deleted = false;
    @OneToMany(mappedBy = "store", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @BatchSize(size = 20)
    @SQLRestriction("deleted = false")
    @Builder.Default
    private Set<Shift> shifts = new HashSet<>();
    @ManyToMany(mappedBy = "preferredStores")
    @BatchSize(size = 20)
    @Builder.Default
    private Set<Employee> preferredEmployees = new HashSet<>();
}
