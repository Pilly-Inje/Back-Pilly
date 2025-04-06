package com.inje.pilly.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SideEffectPredictRequestDTO {
    private Long userId;
    private Long medicineId;
    private String medicineName;
    private int fatigueLevel;
    private int dizzinessLevel;
    private int moodEncoded;
    private float sleepHours;
}
