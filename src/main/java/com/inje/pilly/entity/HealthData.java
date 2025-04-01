package com.inje.pilly.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="health_data")
public class HealthData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="health_data_id")
    private Long healthDataId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "fatigue_level", nullable = false)
    private int fatigueLevel;

    @Column(name = "dizziness_level", nullable = false)
    private int dizzinessLevel;

    @Column(name = "mood", nullable = false, length = 50)
    private String mood;

    @Column(name = "sleep_hours", nullable = false)
    private float sleepHours;

}
