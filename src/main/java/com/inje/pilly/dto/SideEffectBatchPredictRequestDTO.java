package com.inje.pilly.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SideEffectBatchPredictRequestDTO {
//    private Long userId;
//    private int fatigueLevel;
//    private int dizzinessLevel;
//    private int moodEncoded;
//    private float sleepHours;
//    private List<MedicinePredictionInput> medicines;
//
//    public SideEffectBatchPredictRequestDTO(Long userId, List<MedicinePredictionInput> inputList, int fatigueLevel, int dizzinessLevel, int moodEncoded, float sleepHours) {
//    }

    private List<SideEffectPredictRequestDTO> data;
}
