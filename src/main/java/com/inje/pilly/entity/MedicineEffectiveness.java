package com.inje.pilly.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "medicine_effectiveness")
@Data
public class MedicineEffectiveness {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "effectiveness_id")
    private Long effectivenessId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "medicine_id",nullable = false,foreignKey = @ForeignKey(name="fk_medicine"))
    private Medicine medicine;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "effect_level")
    private int effectLevel;

    @Column(name = "side_effect_occurred")
    private boolean sideEffectOccurred;

    @Column(name = "side_effects",columnDefinition = "TEXT")
    private String sideEffects; //json문자

    @Column(columnDefinition = "TEXT")
    private String comments;
}
