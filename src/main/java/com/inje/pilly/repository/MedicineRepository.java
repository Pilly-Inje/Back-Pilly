package com.inje.pilly.repository;

import com.inje.pilly.entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MedicineRepository extends JpaRepository<Medicine,Long> {
    @Query("SELECT m.medicineName FROM Medicine m")
    List<String> findAllMedicineNames();

    Optional<Medicine> findByMedicineName(String medicineName);

    //@Query("SELECT m.medicineId FROM Medicine m")
    Optional<Medicine> findByMedicineId(Long medicineId);

    List<Medicine> findByMedicineNameContainingIgnoreCase(String medicineName);
}
