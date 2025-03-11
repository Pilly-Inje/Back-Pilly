package com.inje.pilly.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name="prescription_medicine")
public class PrescriptionMedicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_medicine_id")
    private Long prescriptionMedicineId;

    @ManyToOne
    @JoinColumn(name = "prescription_id", nullable = false, foreignKey = @ForeignKey(name = "fk_prescription_medicine_prescription"))
    private Prescription prescription;

    @ManyToOne
    @JoinColumn(name = "medicine_id", foreignKey = @ForeignKey(name = "fk_prescription_medicine_medicine"), nullable = true)
    private Medicine medicine;

    @Column(name = "predicted_side_effects", columnDefinition = "TEXT")
    private String predictedSideEffects;

}
