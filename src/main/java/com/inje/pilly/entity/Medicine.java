package com.inje.pilly.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="medicine")
@NoArgsConstructor
@AllArgsConstructor
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="medicine_id")
    private Long medicineId;

    @Column(name="medicine_name", columnDefinition = "TEXT", nullable = false)
    private String medicineName;

    @Column(columnDefinition = "TEXT")
    private String effect;

    @Column(columnDefinition = "TEXT")
    private String dosage;

    @Column(columnDefinition = "TEXT")
    private String caution;

    @Column(name = "medicine_image")
    private String medicineImg;
}