package com.inje.pilly.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SideEffectPredictResponseDTO {
    private Long medicineId;
    private String medicineName;
    private Map<String, Double> probabilities;
    private String feedback;
}
