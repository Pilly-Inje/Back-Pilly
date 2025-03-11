package com.inje.pilly.repository;

import com.inje.pilly.entity.PrescriptionMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrescriptionMedicineRepository extends JpaRepository<PrescriptionMedicine,Long> {
    @Query("SELECT pm FROM PrescriptionMedicine pm LEFT JOIN pm.medicine m WHERE pm.prescription.prescriptionId = :prescriptionId")
    List<PrescriptionMedicine> findByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

    List<PrescriptionMedicine> findByPrescription_PrescriptionId(Long prescriptionId);

    @Query("SELECT COUNT(pm) FROM PrescriptionMedicine pm WHERE pm.prescription.prescriptionId = :prescriptionId")
    int countByPrescriptionId(@Param("prescriptionId") Long prescriptionId);

    @Query("SELECT pm.medicine.medicineId FROM PrescriptionMedicine pm WHERE pm.prescription.prescriptionId = :prescriptionId")
    List<Long> findMedicineIdsByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
