package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SideEffectTrainRecordDTO {
    private Long userId;
    private Long medicineId;
    private int fatigueLevel;
    private int dizzinessLevel;
    private int moodEncoded;
    private float sleepHours;
    private boolean headache;
    private boolean stomachache;
    private boolean rash;
    private boolean vomiting;
    private boolean itchiness;

    public SideEffectTrainRecordDTO(Long userId, Long medicineId, int fatigueLevel, int dizzinessLevel, String mood, float sleepHours, boolean sideEffectOccurred, List<String> sideEffects) {
    }
}
