package com.abanoj.scheman.employee.entity;

import com.abanoj.scheman.auth.entity.User;
import com.abanoj.scheman.shared.BaseEntity;
import com.abanoj.scheman.shift.entity.ShiftType;
import com.abanoj.scheman.shiftassignment.entity.ShiftAssignment;
import com.abanoj.scheman.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

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
    @Enumerated(EnumType.STRING)
    private ShiftType preferredShift;
    @ManyToMany
    @JoinTable(
            name = "employee_store",
            joinColumns = @JoinColumn(name = "employee_id"),
            inverseJoinColumns = @JoinColumn(name = "store_id")
    )
    List<Store> preferredStores = new ArrayList<>();
    private Integer weeklyContractedHours;

    public void addStore(Store store){
        this.preferredStores.add(store);
        store.getPreferredEmployees().add(this);
    }

    public void removeStore(Store store){
        this.preferredStores.remove(store);
        store.getPreferredEmployees().remove(this);
    }

}
