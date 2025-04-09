package com.inje.pilly.repository;

import com.inje.pilly.dto.HealthDataRequestDTO;
import com.inje.pilly.dto.SideEffectTrainRecordDTO;
import com.inje.pilly.entity.MedicineEffectiveness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicineEffectivenessRepository extends JpaRepository<MedicineEffectiveness,Long> {
    List<MedicineEffectiveness> findAllByMedicine_MedicineId(Long medicineId);

    @Query(value = """
        SELECT
            p.user_id AS userId,
            pm.medicine_id AS medicineId,
            :fatigueLevel AS fatigueLevel,
            :dizzinessLevel AS dizzinessLevel,
            CASE :mood
                WHEN '나쁨' THEN 0
                WHEN '보통' THEN 1
                WHEN '좋음' THEN 2
            END AS moodEncoded,
            :sleepHours AS sleepHours,
            JSON_CONTAINS(me.side_effects, '\"두통\"') AS headache,
            JSON_CONTAINS(me.side_effects, '\"복통\"') AS stomachache,
            JSON_CONTAINS(me.side_effects, '\"두드러기\"') AS rash,
            JSON_CONTAINS(me.side_effects, '\"구토\"') AS vomiting,
            JSON_CONTAINS(me.side_effects, '\"가려움증\"') AS itchiness
        FROM prescription_medicine pm
        JOIN prescription p ON pm.prescription_id = p.prescription_id
        JOIN medicine_effectiveness me 
            ON me.user_id = p.user_id
           AND me.medicine_id = pm.medicine_id
           AND me.record_date = :recordDate
        WHERE p.user_id = :userId
    """, nativeQuery = true)
    List<SideEffectTrainRecordDTO> findTrainingDataByUserIdAndRecordDate(
            @Param("userId") Long userId,
            @Param("recordDate") String recordDate,
            @Param("fatigueLevel") int fatigueLevel,
            @Param("dizzinessLevel") int dizzinessLevel,
            @Param("mood") String mood,
            @Param("sleepHours") float sleepHours
    );
    @Query(value = """
    SELECT 
        me.user_id AS userId,
        me.medicine_id AS medicineId,
        h.fatigue_level AS fatigueLevel,
        h.dizziness_level AS dizzinessLevel,
        h.mood AS mood,
        h.sleep_hours AS sleepHours,
        me.side_effect_occurred AS sideEffectOccurred,
        me.side_effects AS sideEffects,
        me.record_date AS recordDate
    FROM medicine_effectiveness me
    JOIN health_data h ON me.user_id = h.user_id
    AND DATE(h.record_date) = DATE(me.record_date)
    WHERE me.side_effect_occurred = true
""", nativeQuery = true)
    List<Object[]> findTrainableSideEffectDataRaw();
}
