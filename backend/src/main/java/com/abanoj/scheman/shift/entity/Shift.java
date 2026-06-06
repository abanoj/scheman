package com.abanoj.scheman.shift.entity;

import com.abanoj.scheman.shared.BaseEntity;
import com.abanoj.scheman.store.entity.Store;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@Table(name = "shifts", indexes = {
        @Index(name = "idx_shifts_store_id", columnList = "store_id"),
        @Index(name = "idx_shifts_group_id", columnList = "group_id")
})
public class Shift extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;
    @Column(nullable = false)
    private LocalTime startTime;
    @Column(nullable = false)
    private LocalTime endTime;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "shift_available_days", joinColumns = @JoinColumn(name = "shift_id"))
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<DayOfWeek> availableDays = new HashSet<>();

    private UUID groupId;
    private LocalDate effectiveFrom;
    @Builder.Default
    private LocalDate effectiveTo = null;
    @Builder.Default
    private boolean deleted = false;

    @Enumerated(EnumType.STRING)
    private ShiftType shiftType;

    @Transient
    public boolean isCrossesMidnight() {
        return startTime.isAfter(endTime);
    }

    @Transient
    public double getTotalHours(){
        Duration duration = Duration.between(startTime, endTime);
        if(duration.isNegative()){
            duration = duration.plusDays(1);
        }
        return duration.toMinutes() / 60.0;
    }
}
