package com.abanoj.scheman.shift.entity;

import com.abanoj.scheman.shared.BaseEntity;
import com.abanoj.scheman.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "shifts")
public class Shift extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
    private LocalTime startTime;
    private LocalTime endTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shift_available_days", joinColumns = @JoinColumn(name = "shift_id"))
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<DayOfWeek> availableDays = new HashSet<>();

    private UUID groupId;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo = null;
    @Builder.Default
    private boolean deleted = false;

    @Transient
    public boolean isCrossesMidnight() {
        return startTime != null && endTime != null && startTime.isAfter(endTime);
    }
}
