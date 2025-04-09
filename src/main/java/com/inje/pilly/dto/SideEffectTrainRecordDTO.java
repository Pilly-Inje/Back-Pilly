package com.inje.pilly.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SideEffectTrainRecordDTO {

    private Long userId;
    private Long medicineId;
    private int fatigueLevel;
    private int dizzinessLevel;
    private String mood;
    private float sleepHours;
    private boolean sideEffectOccurred;
    private List<String> sideEffects;
    private String recordDate;
}
