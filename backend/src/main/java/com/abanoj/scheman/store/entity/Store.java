package com.abanoj.scheman.store.entity;

import com.abanoj.scheman.shared.BaseEntity;
import com.abanoj.scheman.shift.entity.Shift;
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
@Table(name = "store")
public class Store extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private String name;
    private String address;
    private Boolean is24h;
    private String phone;
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Shift> shifts = new HashSet<>();
}
