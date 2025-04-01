package com.inje.pilly.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medicine")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medicine_id")
    private Long medicineId;

    @Column(name = "medicine_name", nullable = false, columnDefinition = "TEXT")
    private String medicineName;  // 약 이름

    @Column(name = "effect", columnDefinition = "TEXT")
    private String effect; // 효능

    @Column(name = "dosage", columnDefinition = "TEXT")
    private String dosage; // 복용법

    @Column(name = "medicine_image", length = 500)
    private String medicineImage; // 약 이미지

    @Column(name = "caution", columnDefinition = "TEXT")
    private String caution; // 주의사항

    @Column(name = "origin_image_url")
    private String originImageUrl;

}
