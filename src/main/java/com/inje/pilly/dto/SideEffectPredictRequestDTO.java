package com.inje.pilly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SideEffectPredictRequestDTO {
    private Long userId;
    private Long medicineId;
    private int fatigueLevel;
    private int dizzinessLevel;
    private int moodEncoded;
    private float sleepHours;
}
