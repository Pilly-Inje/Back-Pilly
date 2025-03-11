package com.inje.pilly.repository;

import com.inje.pilly.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription,Long>{
    Optional<Prescription> findByPrescriptionId(Long prescriptionId);
    //Optional<Prescription> findByPrescriptionIdAndUserId(Long prescriptionId, Long userId);
    @Query("SELECT p FROM Prescription p WHERE p.user.userId = :userId ORDER BY p.startDate DESC")
    List<Prescription> findByUserIdOrderByStartDateDesc(@Param("userId")Long userId);

    @Query("SELECT p FROM Prescription p WHERE p.user.userId = :userId AND p.startDate <= :today AND p.endDate >= :today")
    List<Prescription> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("today") LocalDate today);
}
